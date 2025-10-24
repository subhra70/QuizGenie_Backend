package com.subhrashaw.QuizGeneratorBackend.Service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RazorPayService {
    @Value("${razorpay.api.key}")
    private String razorpayApiKey;

    @Value("${razorpay.api.secret}")
    private String razorpaySecret;

    public String createOrder(int amount,int id) throws RazorpayException {
        RazorpayClient razorpayClient=new RazorpayClient(razorpayApiKey,razorpaySecret);
        JSONObject orderRequest=new JSONObject();
        orderRequest.put("amount",amount*100); // converting into rupees to paisa
        orderRequest.put("currency","INR");
        orderRequest.put("receipt","receipt_"+id+amount);
        orderRequest.put("payment_capture", 1);
        Order order=razorpayClient.orders.create(orderRequest);
        return order.toString();
    }
}
