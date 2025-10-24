package com.subhrashaw.QuizGeneratorBackend.Controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.subhrashaw.QuizGeneratorBackend.DTO.PaymentRequest;
import com.subhrashaw.QuizGeneratorBackend.Service.JwtService;
import com.subhrashaw.QuizGeneratorBackend.Service.QuizService;
import com.subhrashaw.QuizGeneratorBackend.Service.RazorPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
public class PaymentController {
    @Autowired
    private RazorPayService razorPayService;

    @Autowired
    private JwtService jwtService;

    @Value("${razorpay.api.secret}")
    private String razorpaySecret;
    @Value("${razorpay.api.key}")
    private String razorpayApiKey;

    @Autowired
    private QuizService quizService;

    @PostMapping("purchase/{amount}")
    public ResponseEntity<String> createOrder(@RequestHeader("Authorization") String auth, @PathVariable("amount") int amount, @RequestBody PaymentRequest paymentRequest) throws RazorpayException {
        if(auth==null || !auth.startsWith("Bearer "))
        {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token=auth.substring(7);
        String email= jwtService.extractUserName(token);
        if (!jwtService.validateToken(token, email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String response= razorPayService.createOrder(amount,paymentRequest.getId());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PostMapping("verifyPayment")
    public ResponseEntity<HttpStatus> verifyPayment(@RequestHeader("Authorization") String auth, @RequestBody Map<String,String> data) {
        try {
            if (auth == null || !auth.startsWith("Bearer ")) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            String token = auth.substring(7);
            String email = jwtService.extractUserName(token);
            if (!jwtService.validateToken(token, email)) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            String orderId = data.get("razorpay_order_id");
            String paymentId = data.get("razorpay_payment_id");
            String signature = data.get("razorpay_signature");
            String secret = razorpaySecret; // use env variable in real project
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes());
            String generatedSignature = new String(org.apache.commons.codec.binary.Hex.encodeHex(hash));
            if (generatedSignature.equals(signature)) {
                RazorpayClient razorpayClient=new RazorpayClient(razorpayApiKey,razorpaySecret);
                Order order=razorpayClient.orders.fetch(orderId);
                int planId=Integer.parseInt(order.get("receipt").toString().substring(8,10));
                int amount=Integer.parseInt(order.get("receipt").toString().substring(10));
                quizService.handlePurchase(email,planId,amount);
                return new ResponseEntity<>(HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
