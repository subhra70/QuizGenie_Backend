package com.subhrashaw.QuizGeneratorBackend.Service;

import com.subhrashaw.QuizGeneratorBackend.DAO.QuizClassRepo;
import com.subhrashaw.QuizGeneratorBackend.DAO.QuizMarksRepo;
import com.subhrashaw.QuizGeneratorBackend.DAO.QuizQuestionsRepo;
import com.subhrashaw.QuizGeneratorBackend.DAO.QuizResultRepo;
import com.subhrashaw.QuizGeneratorBackend.DTO.QuizResponse;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizClass;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizMarks;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizQuestion;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.List;


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
        if(format.equals("JECA"))
        {
            return quizMarksRepo.findByTypeAndMarkAndNegMark(type,mark,0.25);
        }
        return quizMarksRepo.findByTypeAndMarkAndNegMark(type,mark,0.33);
    }
    public void saveQuestions(String email,List<QuizQuestion> list,int duration,int fullMarks)
    {
        LocalDate currentDate=LocalDate.now();
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String date= currentDate.format(formatter);
        QuizClass quiz=new QuizClass();
        quiz.setEmail(email);
        quiz.setQuizQuestion(list);
        quiz.setDuration(duration);
        quiz.setFullMarks(fullMarks);
        quiz.setPerformed(false);
        quiz.setLocked(false);
        quiz.setPassword(null);
        quizClassRepo.save(quiz);
        QuizResult result=new QuizResult();
        result.setQuizClass(quiz);
        result.setObtainedMark(0);
        result.setEmail(email);
        result.setDate(date);
        resultRepo.save(result);
    }

    public QuizClass getQuestionSet(int id, String email) {
        return quizClassRepo.findByIdAndEmail(id,email);
    }

    public boolean calResult(QuizResponse response, int uid,String email) {
        QuizClass quizClass=quizClassRepo.findById(uid).orElse(new QuizClass((-1)));
        LocalDate currentDate=LocalDate.now();
        DateTimeFormatter formatter=DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String date= currentDate.format(formatter);
        if(quizClass.getId()==-1)
        {
            return false;
        }
        List<QuizQuestion> questionSet=quizClass.getQuizQuestion();
        double marks=0.0;
        for (QuizQuestion question:questionSet)
        {
            if(question.getMarks().getType().equals("MCQ")&& response.getQid()==question.getId())
            {
                if(!response.getAnswer().trim().equals("")&&response.getAnswer().trim().equals(question.getAnswer().trim()))
                {
                    marks+=question.getMarks().getMark();
                }
                else{
                    marks-=question.getMarks().getNegMark();
                }
            }
            else if(!response.getAnswer().trim().equals("")&&question.getMarks().getType().equals("MSQ") && response.getQid()== question.getId())
            {
                String answers[]=response.getAnswer().split(",");
                String savedAnswer[]=question.getAnswer().split(",");
                int match=0;
                if(answers.length== savedAnswer.length)
                {
                    for(int i=0;i< answers.length;i++)
                    {
                        String curr=answers[i];
                        for(int j=0;j< savedAnswer.length;j++)
                        {
                            if(savedAnswer[j].trim().equals(curr.trim()))
                            {
                                match++;
                            }
                        }
                    }
                    double val=(question.getMarks().getMark()*(match/savedAnswer.length)*100.0)/100.0;
                    marks+=val;
                }

            }
            else if(!response.getAnswer().trim().equals("")&&question.getMarks().getType().equals("NAT"))
            {
                double respAns=(Double.parseDouble(response.getAnswer())*100.0)/100.0;
                double answered=(Double.parseDouble(question.getAnswer())*100.0)/100.0;
                if(respAns==answered)
                {
                    marks+=question.getMarks().getMark();
                }
            }
        }
        QuizResult result=resultRepo.findByQuizClass(quizClass);
        if(result==null)
        {
            return false;
        }
        quizClass.setPerformed(true);
        result.setObtainedMark(marks);
        result.setQuizClass(quizClass);
        resultRepo.save(result);
        return true;
    }

    public List<QuizResult> getResult(String email) {
        return resultRepo.findAllByEmail(email);
    }
}
