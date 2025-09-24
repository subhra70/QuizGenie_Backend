package com.subhrashaw.QuizGeneratorBackend.Service;

import com.subhrashaw.QuizGeneratorBackend.DAO.*;
import com.subhrashaw.QuizGeneratorBackend.DTO.ManualQuizQuestion;
import com.subhrashaw.QuizGeneratorBackend.DTO.QuizRequest;
import com.subhrashaw.QuizGeneratorBackend.DTO.QuizResponse;
import com.subhrashaw.QuizGeneratorBackend.Model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.*;


@Service
public class QuizService {
    @Autowired
    private QuizMarksRepo quizMarksRepo;
    @Autowired
    private QuizQuestionsRepo quizQuestionsRepo;
    @Autowired
    private QuizClassRepo quizClassRepo;
    @Autowired
    private QuizResultRepo resultRepo;
    @Autowired
    private UserRepo userRepo;
    public void setMarks()
    {
        QuizMarks quizMarks1=new QuizMarks("MCQ",1,0.33);
        QuizMarks quizMarks2=new QuizMarks("MCQ",2,0.66);
        QuizMarks quizMarks3=new QuizMarks("MSQ",1,0);
        QuizMarks quizMarks4=new QuizMarks("MSQ",2,0);
        QuizMarks quizMarks5=new QuizMarks("NAT",1,0);
        QuizMarks quizMarks6=new QuizMarks("NAT",2,0);
        QuizMarks quizMarks7=new QuizMarks("MCQ",12,3);
        QuizMarks quizMarks8=new QuizMarks("MCQ",6,1.5);
        QuizMarks quizMarks9=new QuizMarks("MCQ",4,1);
        QuizMarks quizMarks10=new QuizMarks("MCQ",5,1);
        QuizMarks quizMarks11=new QuizMarks("MCQ",1,0.25);
        quizMarksRepo.save(quizMarks1);
        quizMarksRepo.save(quizMarks2);
        quizMarksRepo.save(quizMarks3);
        quizMarksRepo.save(quizMarks4);
        quizMarksRepo.save(quizMarks5);
        quizMarksRepo.save(quizMarks6);
        quizMarksRepo.save(quizMarks7);
        quizMarksRepo.save(quizMarks8);
        quizMarksRepo.save(quizMarks9);
        quizMarksRepo.save(quizMarks10);
        quizMarksRepo.save(quizMarks11);
    }
    public QuizMarks getQuizMarks(String type,int mark,String format)
    {
        if(format.equals("JECA") && type.equals("MCQ"))
        {
            return quizMarksRepo.findByTypeAndMarkAndNegMark(type,mark,0.25);
        }
        return quizMarksRepo.findByTypeAndMark(type,mark);
    }
    public void saveQuestions(String email,List<QuizQuestion> list,int duration,int fullMarks)
    {
        LocalDate currentDate=LocalDate.now();
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String date= currentDate.format(formatter);
        QuizClass quiz=new QuizClass();
        quiz.setQuizQuestion(list);
        quiz.setDuration(duration);
        quiz.setFullMarks(fullMarks);
        quiz.setLocked(false);
        quiz.setPassword(null);
        quizClassRepo.save(quiz);
        QuizUsers user=userRepo.findByEmail(email);
        if(user==null)
        {
            return;
        }
        QuizResult result=new QuizResult();
        result.setQuizClass(quiz);
        result.setObtainedMark(0);
        result.setDate(date);
        result.setQuizUser(user);
        result.setRole("User");
        resultRepo.save(result);
    }

    public QuizClass getQuestionSet(int id, String email) {
        return quizClassRepo.findById(id);
    }

    public boolean calResult(List<QuizResponse> response, int uid,String email) {
        QuizClass quizClass=quizClassRepo.findById(uid);
        if(quizClass==null)
        {
            return false;
        }
        List<QuizQuestion> questions=quizClass.getQuizQuestion();
        HashMap<Integer,String[]> map=new HashMap<>();
        double marks=0.0;
        int full_marks=0;
        for(QuizResponse resp:response)
        {
            map.put(resp.getId(), resp.getAnswer());
        }
        for (QuizQuestion q : questions) {
            full_marks+=q.getMarks().getMark();
            if (map.containsKey(q.getId())) {
                String[] answer = map.get(q.getId());

                if ("MCQ".equals(q.getMarks().getType())) {
                    System.out.println(1);
                    if (answer != null && answer.length > 0) {
                        if (q.getAnswer().trim().equals(answer[0].trim())) {
                            marks += q.getMarks().getMark();
                        } else {
                            marks -= q.getMarks().getNegMark();
                        }
                    }
                }
                else if ("MSQ".equals(q.getMarks().getType())) {
                    String[] savedAns = q.getAnswer().split(",");
                    Set<String> set = new HashSet<>();
                    if (answer != null) {
                        for (String ans : answer) set.add(ans.trim());
                    }

                    int correct = 0;
                    boolean wrong = false;
                    for (String ans : savedAns) {
                        if (set.contains(ans.trim())) correct++;
                        else wrong = true;
                    }

                    // If no wrongs, partial marking
                    if (!wrong) {
                        marks += ((double) correct / savedAns.length) * q.getMarks().getMark();
                    }
                }
                else { // Numeric/NAT
                    if (answer != null && answer.length > 0) {
                        double savedAns = Double.parseDouble(q.getAnswer().trim());
                        double respAns = Double.parseDouble(answer[0].trim());
                        if (Math.abs(savedAns - respAns) < 1e-6) {
                            marks += q.getMarks().getMark();
                        }
                    }
                }
            }
        }
        quizClass.setFullMarks(full_marks);
        QuizResult result=resultRepo.findByQuizClass(quizClass);
        if(result==null)
        {
            return false;
        }
        QuizUsers user=userRepo.findByEmail(email);
        quizClassRepo.save(quizClass);
        result.setObtainedMark(marks);
        result.setIsPerformed(true);
        result.setQuizUser(user);
        resultRepo.save(result);
        return true;
    }

    public List<QuizResult> getResult(String email) {
        QuizUsers user=userRepo.findByEmail(email);
        return resultRepo.findAllByQuizUser(user);
    }

    public boolean saveQuiz(QuizRequest details, List<ManualQuizQuestion> questions, String email) {
        LocalDate currentDate=LocalDate.now();
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String date= currentDate.format(formatter);
        QuizClass quizClass=new QuizClass();
        List<QuizQuestion> questionSet=new ArrayList<>();
        for(ManualQuizQuestion q:questions)
        {
            String [] options=q.getOptions();
            QuizMarks mark=quizMarksRepo.findByTypeAndMark(q.getType(),q.getMark());
            questionSet.add(new QuizQuestion(q.getQuestion(),options[0],options[1],options[2],options[3],q.getAnswer(),mark));
        }
        List<QuizQuestion> questions1=quizQuestionsRepo.saveAll(questionSet);
        if(questions1==null || questions1.size()==0)
        {
            return false;
        }
        quizClass.setFullMarks(details.getFullMarks());
        quizClass.setLocked(false);
        quizClass.setPassword(null);
        quizClass.setDuration(details.getDuration());
        quizClass.setQuizQuestion(questions1);
        QuizClass response=quizClassRepo.save(quizClass);
        if(response==null)
        {
            return false;
        }
        QuizResult result=new QuizResult();
        QuizUsers user=userRepo.findByEmail(email);
        if(user==null)
        {
            return false;
        }
        result.setObtainedMark(0.0);
        result.setQuizUser(user);
        result.setDate(date);
        result.setIsPerformed(true);
        result.setRole("Admin");
        result.setQuizClass(response);
        QuizResult status=resultRepo.save(result);
        if(status==null)
        {
            return false;
        }
        return true;
    }
}
