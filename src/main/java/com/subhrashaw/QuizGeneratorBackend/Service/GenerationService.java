package com.subhrashaw.QuizGeneratorBackend.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Candidate;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.subhrashaw.QuizGeneratorBackend.DAO.QuizQuestionsRepo;
import com.subhrashaw.QuizGeneratorBackend.DTO.GenerateResponse;
import com.subhrashaw.QuizGeneratorBackend.DTO.QuizRequest;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizMarks;
import com.subhrashaw.QuizGeneratorBackend.Model.QuizQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.genai.types.Part;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class GenerationService {

    private static final Logger logger = LoggerFactory.getLogger(GenerationService.class);

    @Value("${spring.ai.gemini.vertex.ai.api-key}")
    private String apiKey;

    @Autowired
    private QuizQuestionsRepo quizQuestionsRepo;

    private final QuizService quizService;
    private final ObjectMapper objectMapper;

    @Autowired
    public GenerationService(QuizService quizService) {
        this.quizService = quizService;
        this.objectMapper = new ObjectMapper();
    }

    public GenerateResponse generate(QuizRequest request) {
        String query = getQuery(request);
        if (query == null) {
            logger.warn("Unsupported quiz format: {}", request.getFormat());
            return null;
        }

        logger.info("Generated query for AI model:\n{}", query);

        try {
            Client client = Client.builder().apiKey(apiKey).build();
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", query, null);
            String text=response.text();
            if(text.startsWith("```"))
            {
                int firsNwline=text.indexOf('\n');
                text=text.substring(firsNwline+1);
            }
            if(text.endsWith("```"))
            {
                text=text.substring(0,text.length()-3);
            }
            JsonNode rootNode = objectMapper.readTree(text);
            GenerateResponse generateResponse=new GenerateResponse();
            List<QuizQuestion> questions = new ArrayList<>();
            int fm[]=new int[1];
            rootNode.forEach(node -> {
                QuizQuestion q = createQuizQuestion(node,request.getFormat());
                if (q != null) {
                    quizQuestionsRepo.save(q);
                    questions.add(q);
                    fm[0]+=q.getMarks().getMark();
                }
            });
            generateResponse.setQuestions(questions);
            generateResponse.setFullMarks(fm[0]);
            System.out.println(questions);
            return generateResponse;


        } catch (Exception e) {
            logger.error("An error occurred during quiz generation", e);
            return null;
        }
    }

    private String getQuery(QuizRequest request) {
        return switch (request.getFormat()) {
            case "GATE" -> String.format(
                    "Generate questions for %s examination consists of exactly %d questions of total 100 marks. First 10 questions (5 MCQ 1 marks, 3 MCQ 2 marks and 2 MSQ 2 marks) of General Knowledge (Medium to Hard Level) must come consequtively." +
                            "The remaining 55 questions (85 marks) are from the subject paper and comprise of 19 MCQ 1 mark, 12 MCQ 2 marks, 10 MSQ 1 mark, 9 MSQ 2 marks, 4 NAT type of 1 mark and 5 NAT type of 2 marks." +
                            "NAT questions require a numerical response within the specified answer range." +
                            "Try to generate at least of 1 question from each subjects or topics. " +
                            "User may specifies different subjects then please make sure that all the questions from same subject are not appeared consecutively(except GA), it must be shuffled enough also same type question should not come consequtively. "+
                            "Return the data in a JSON-friendly key-value format. Keys must be 'Question', 'Options' (comma-separated), " +
                            "'Answers' (comma-separated for MSQ), 'Type', and 'Marks'. " +
                            "Additional details: %s",
                    request.getFormat(), request.getTotalQuestion(), request.getDescription());

            case "NIMCET" -> String.format(
                    "Generate a question set for the %s exam. Generate exactly %d questions," +
                            "Mathematics (50 questions, 12 marks each), Analytical Ability & Logical Reasoning (40 questions, 6 marks each), " +
                            "Computer Awareness (20 questions, 6 marks each), and General English (10 questions, 4 marks each).User may specifies different subjects then please make sure that all the questions from same subject are not appeared consecutively, it must be shuffled enough.For code snippet try to follow proper indentation" +
                            "All questions are single-answer MCQs. Return a JSON-friendly key-value format with 'Question', 'Options' (comma-separated), " +
                            "'Answers', 'Type' (MCQ), and 'Marks'. " +
                            "Additional details: %s",
                    request.getFormat(), request.getTotalQuestion(), request.getDescription());

            case "JECA" -> String.format(
                    "Generate a question set for the %s exam. Generate exactly %d " +
                            "questions. Question set must includes exactly 80 MCQs (1 mark each) and exactly 20 MSQs (2 marks each) with multiple answers. The 20 MSQs must be after the 80 MCQs. User may specifies different subjects then please make sure that all the questions from same subject are not appeared consecutively, it must be shuffled enough. " +
                            "Return a JSON-friendly key-value format with 'Question', 'Options' (comma-separated), " +
                            "'Answers' (comma-separated for MSQ), 'Type', and 'Marks'. " +
                            "Additional details: %s",
                    request.getFormat(), request.getTotalQuestion(), request.getDescription());

            case "CUET(UG)" -> String.format(
                    "Generate a question set for the %s exam. Generate exactly %d questions. " +
                            "The set should contain 50 questions on English (Reading Comprehension, Vocabulary, Grammar), " +
                            "50 on a specific subject (based on NCERT Class 12 syllabus), and 50 on General Awareness (GK, Current Affairs, Reasoning). " +
                            "All questions are single-answer MCQs, each worth 5 marks. User may specifies different subjects then please make sure that all the questions from same subject are not appeared consecutively, it must be shuffled enough.For code snippet try to follow proper indentation" +
                            "Return a JSON-friendly key-value format with 'Question', 'Options' (comma-separated), 'Answers', 'Type', and 'Marks' " +
                            "Additional details: %s",
                    request.getFormat(), request.getTotalQuestion(), request.getDescription());

            case "CUET(PG)" -> String.format(
                    "Generate a question set of format CUET(PG). Total no of questions: %d. " +
                            "Each question is of MCQ type and carries 4 marks. User may specifies different subjects then please make sure that all the questions from same subject are not appeared consecutively, it must be shuffled enough.For code snippet try to follow proper indentation. At least one question must have from each topic or subject.Your task is to return question, 4 options(separated by comma), answer, type and marks. Other Details: %s",
                    request.getTotalQuestion(), request.getDescription());

            case "Other" ->
                String.format(
                        "Generate a question set of exactly of %d questions. " +
                                "Total MCQ of mark 1 is %d, total MCQ of mark 2 is %d, total MSQ of mark 1 is %d, total MSQ of mark 2 is %d,total NAT of mark 1 is %d and total NAT of mark 2 is %d. User may specifies different subjects then please make sure that all the questions from same subject are not appeared consecutively, it must be shuffled enough.For code snippet try to follow proper indentation. Your task is to return question, 4 options(separated by comma), answer, type and marks. For type NAT don't need to give options. Other Details: %s",
                        request.getTotalQuestion(),request.getMcq1(),request.getMcq2(),request.getMsq1(),request.getMsq2(),request.getNat1(),request.getNat2(),request.getDescription());
            default -> {
                yield null;
            }
        };
    }

    private QuizQuestion createQuizQuestion(JsonNode obj,String format) {
        try {
            String question = obj.path("Question").asText();
            String optionsStr = obj.path("Options").asText("");
            String[] options = optionsStr.isEmpty() ? new String[0] : optionsStr.split(",");
            String type = obj.path("Type").asText().toUpperCase();
            int marks = obj.path("Marks").asInt();
            String answer = obj.path("Answers").asText();

            if (question.isEmpty() || type.isEmpty() || answer.isEmpty()) {
                logger.warn("Skipping malformed question due to missing key data: Question='{}', Type='{}'", question, type);
                return null;
            }

            QuizMarks qmarks = quizService.getQuizMarks(type, marks,format);

            boolean isOptionBased = type.equalsIgnoreCase("MCQ") || type.equalsIgnoreCase("MSQ");
            if (isOptionBased && options.length < 4) {
                logger.warn("Skipping question '{}' due to insufficient options: {}", question, options.length);
                return null;
            }

            return new QuizQuestion(
                    question,
                    (options.length > 0) ? options[0].trim() : "",
                    (options.length > 1) ? options[1].trim() : "",
                    (options.length > 2) ? options[2].trim() : "",
                    (options.length > 3) ? options[3].trim() : "",
                    answer,
                    qmarks);

        } catch (Exception e) {
            logger.error("Error parsing a quiz question from JSON: {}", obj.toString(), e);
            return null;
        }
    }
}
