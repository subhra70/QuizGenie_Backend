package com.subhrashaw.QuizGeneratorBackend.Service;

import com.subhrashaw.QuizGeneratorBackend.DAO.*;
import com.subhrashaw.QuizGeneratorBackend.DTO.*;
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
    @Autowired
    private ProblemRepo problemRepo;
    @Autowired
    private UserTrackRepo trackRepo;
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
        else if(format.equals("Other") && type.equals("MCQ"))
        {
            if(mark==1) {
                return quizMarksRepo.findByTypeAndMarkAndNegMark(type, mark, 0.25);
            }
            else
            {
                return quizMarksRepo.findByTypeAndMarkAndNegMark(type,mark,0.5);
            }
        }
        else if(type.equals("MCQ") && format.equals("GATE"))
        {
            if (mark==1) {
                return quizMarksRepo.findByTypeAndMarkAndNegMark(type, mark, 0.33);
            }
            else{
                return quizMarksRepo.findByTypeAndMarkAndNegMark(type,mark,0.66);
            }
        }
        return quizMarksRepo.findByTypeAndMark(type,mark);
    }
    public void saveQuestions(String email,List<QuizQuestion> list,int duration,int fullMarks,boolean isNegative)
    {
        LocalDate currentDate=LocalDate.now();
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String date= currentDate.format(formatter);
        QuizClass quiz=new QuizClass();
        quiz.setQuizQuestion(list);
        quiz.setDuration(duration);
        quiz.setFullMarks(fullMarks);
        quiz.setNegAllow(isNegative);
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
        result.setRole("Admin");
        resultRepo.save(result);
    }

    public QuizClass getQuestionSet(int id, String email) {
        return quizClassRepo.findById(id);
    }

    public boolean calResult(List<QuizResponse> response, int uid,String email) {
        QuizClass quizClass=quizClassRepo.findById(uid);
        boolean neg=quizClass.isNegAllow();
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
                    if (answer != null && answer.length > 0) {
                        if (q.getAnswer().trim().equals(answer[0].trim())) {
                            marks += q.getMarks().getMark();
                        } else {
                            if(neg) {
                                marks -= q.getMarks().getNegMark();
                            }
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
        QuizUsers user=userRepo.findByEmail(email);
        QuizResult result=resultRepo.findByQuizClassAndQuizUser(quizClass,user);
        if(result==null)
        {
            return false;
        }
        quizClassRepo.save(quizClass);
        result.setObtainedMark(marks);
        result.setPerformed(true);
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
            QuizMarks mark=quizMarksRepo.findByTypeAndMarkAndNegMark(q.getType(),q.getMark(),0.25);
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
        result.setPerformed(true);
        result.setRole("Admin");
        result.setQuizClass(response);
        QuizResult status=resultRepo.save(result);
        if(status==null)
        {
            return false;
        }
        return true;
    }

    public QuizClass getQuizClass(int qid) {
        return quizClassRepo.findById(qid);
    }

    public void saveQuizClass(QuizClass quizClass) {
        quizClassRepo.save(quizClass);
    }

    public void deleteResult(int id) {
        resultRepo.deleteById(id);
    }

    public QuizResult saveToHistory(QuizUsers user, QuizClass quizClass) {
        List<QuizResult> list=resultRepo.findAllByQuizUser(user);
        for(QuizResult temp:list)
        {
            if(temp.getQuizClass().getId()== quizClass.getId())
            {
                return null;
            }
        }
        QuizResult result=new QuizResult();
        LocalDate currentDate=LocalDate.now();
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String date= currentDate.format(formatter);
        result.setPerformed(false);
        result.setQuizUser(user);
        result.setQuizClass(quizClass);
        result.setRole("User");
        result.setPerformed(false);
        result.setObtainedMark(0);
        result.setDate(date);
        QuizResult response=resultRepo.save(result);
        return response;
    }

    public boolean updateQuizClass(QuizClass quizClass, EditRequestFormat requestFormat) {
        quizClass.setFullMarks(requestFormat.getFullMarks());
        quizClass.setDuration(requestFormat.getDuration());
        HashMap<Integer, EditQuestionFormat> map=new HashMap<>();
        for(EditQuestionFormat q:requestFormat.getQuestionSet())
        {
            map.put(q.getQuestionsId(),q);
        }
        for(QuizQuestion q: quizClass.getQuizQuestion())
        {
            if(map.containsKey(q.getId()))
            {
                EditQuestionFormat format=map.get(q.getId());
                q.setQuestion(format.getQuestion());
                q.setOption1(format.getOptions()[0]);
                q.setOption2(format.getOptions()[1]);
                q.setOption3(format.getOptions()[2]);
                q.setOption4(format.getOptions()[3]);
                q.setAnswer(format.getAnswer());
            }
        }
        quizClassRepo.save(quizClass);
        return true;
    }

    public List<QuizResult> getAllResultByQuiz(QuizClass quizClass) {
        List<QuizResult> result = resultRepo.findByQuizClassSorted(quizClass);
        result.removeIf(r -> !r.isPerformed());
        return result;
    }

    public void submitQuery(QuizUsers users, ProblemDTO raisedQuery) {
        Problems problems=new Problems();
        LocalDate currentDate=LocalDate.now();
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String date= currentDate.format(formatter);
        problems.setProblem(raisedQuery.getQuery());
        problems.setRaisedDate(date);
        problems.setUser(users);
        problems.setSolveStatus(false);
        problemRepo.save(problems);
    }

    public boolean solveQuery(int id) {
        Problems problems=problemRepo.findById(id).orElse(null);
        if(problems==null)
        {
            return false;
        }
        LocalDate currentDate=LocalDate.now();
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String date= currentDate.format(formatter);
        problems.setSolveDate(date);
        problems.setSolveStatus(true);
        problemRepo.save(problems);
        return true;
    }

    public List<Problems> getALLProblems() {
        return problemRepo.findAll();
    }

    public boolean deleteProblem(int id) {
        problemRepo.deleteById(id);
        return true;
    }

    public boolean handlePurchase(String email,int id) {
        LocalDate currentDate=LocalDate.now();
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String date= currentDate.format(formatter);
        QuizUsers users= userRepo.findByEmail(email);
        System.out.println(1);
        if(users==null)
        {
            return false;
        }
        System.out.println(2);
        TrialTrack userDetails= trackRepo.findByUser(users);
        System.out.println(userDetails.toString());
        if(userDetails==null)
        {
            return false;
        }
        System.out.println("Here");
        if(id==1)
        {
            userDetails.setPremium(true);
            userDetails.setCreateTrial(userDetails.getCreateTrial()+60);
            userDetails.setFreeTrialAutogen(userDetails.getFreeTrialAutogen()+30);
            userDetails.setAmount(userDetails.getAmount()+169);
            userDetails.setMonthDuration(userDetails.getMonthDuration()+1);
            userDetails.setPurchasedDate(date);
        }
        else if(id==2)
        {
            userDetails.setPremium(true);
            userDetails.setCreateTrial(userDetails.getCreateTrial()+200);
            userDetails.setFreeTrialAutogen(userDetails.getFreeTrialAutogen()+100);
            userDetails.setAmount(userDetails.getAmount()+499);
            userDetails.setMonthDuration(userDetails.getMonthDuration()+3);
            userDetails.setPurchasedDate(date);
        }
        else
        {
            userDetails.setPremium(true);
            userDetails.setCreateTrial(userDetails.getCreateTrial()+4999);
            userDetails.setFreeTrialAutogen(userDetails.getFreeTrialAutogen()+4999);
            userDetails.setAmount(userDetails.getAmount()+1199);
            userDetails.setMonthDuration(userDetails.getMonthDuration()+12);
            userDetails.setPurchasedDate(date);
        }
        System.out.println(userDetails.toString());
        trackRepo.save(userDetails);
        return true;
    }
}
