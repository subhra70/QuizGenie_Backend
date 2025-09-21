package com.subhrashaw.QuizGeneratorBackend.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Candidate;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.subhrashaw.QuizGeneratorBackend.DAO.QuizQuestionsRepo;
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

    public List<QuizQuestion> generate(QuizRequest request) {
        String query = getQuery(request);
        if (query == null) {
            logger.warn("Unsupported quiz format: {}", request.getFormat());
            return Collections.emptyList();
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
            List<QuizQuestion> questions = new ArrayList<>();
            rootNode.forEach(node -> {
                QuizQuestion q = createQuizQuestion(node,request.getFormat());
                if (q != null) {
                    quizQuestionsRepo.save(q);
                    questions.add(q);
                }
            });
            return questions;


        } catch (Exception e) {
            logger.error("An error occurred during quiz generation", e);
            return Collections.emptyList();
        }
    }

    private String getQuery(QuizRequest request) {
        return switch (request.getFormat()) {
            case "GATE" -> String.format(
                    "Generate a question set for the %s exam. Total questions: %d. The set should include " +
                            "MCQs (1 & 2 marks, single answer), MSQs (1 & 2 marks, multiple answers), and NATs (1 & 2 marks, no options). " +
                            "Return the data in a JSON-friendly key-value format. Keys must be 'Question', 'Options' (comma-separated), " +
                            "'Answers' (comma-separated for MSQ), 'Type', and 'Marks'. " +
                            "Additional details: %s",
                    request.getFormat(), request.getTotalQuestion(), request.getDescription());

            case "NIMCET" -> String.format(
                    "Generate a question set for the %s exam. Total questions: %d. " +
                            "Mathematics (50 questions, 12 marks each), Analytical Ability & Logical Reasoning (40 questions, 6 marks each), " +
                            "Computer Awareness (20 questions, 6 marks each), and General English (10 questions, 4 marks each). " +
                            "All questions are single-answer MCQs. Return a JSON-friendly key-value format with 'Question', 'Options' (comma-separated), " +
                            "'Answers', 'Type' (MCQ), and 'Marks'. " +
                            "Additional details: %s",
                    request.getFormat(), request.getTotalQuestion(), request.getDescription());

            case "JECA" -> String.format(
                    "Generate a question set for the %s exam. Total questions: %d. " +
                            "This includes 5 MCQs (1 mark each) and 2 MSQs (2 marks each) with multiple answers. The 2 MSQs must be after the 5 MCQs. " +
                            "Return a JSON-friendly key-value format with 'Question', 'Options' (comma-separated), " +
                            "'Answers' (comma-separated for MSQ), 'Type', and 'Marks'. " +
                            "Additional details: %s",
                    request.getFormat(), request.getTotalQuestion(), request.getDescription());

            case "CUET(UG)" -> String.format(
                    "Generate a question set for the %s exam. Total questions: %d. " +
                            "The set should contain 50 questions on English (Reading Comprehension, Vocabulary, Grammar), " +
                            "50 on a specific subject (based on NCERT Class 12 syllabus), and 50 on General Awareness (GK, Current Affairs, Reasoning). " +
                            "All questions are single-answer MCQs, each worth 5 marks. " +
                            "Return a JSON-friendly key-value format with 'Question', 'Options' (comma-separated), 'Answers', 'Type', and 'Marks' " +
                            "Additional details: %s",
                    request.getFormat(), request.getTotalQuestion(), request.getDescription());

            case "CUET(PG)" -> String.format(
                    "Generate a question set of format CUET(PG). Total no of questions: %d. " +
                            "Each question is of MCQ type and carries 4 marks. Your task is to return question, 4 options(separated by comma), answer, type and marks. Other Details: %s",
                    request.getTotalQuestion(), request.getDescription());

            case "Other" ->
                String.format(
                        "Generate a question set of Total no of questions: %d. " +
                                "Total MCQ of mark 1 is %d, total MCQ of mark 2 is %d, total MSQ of mark 1 is %d, total MSQ of mark 2 is %d,total NAT of mark 1 is %d and total NAT of mark 2 is %d .Your task is to return question, 4 options(separated by comma), answer, type and marks. For type NAT don't need to give options. Other Details: %s",
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
            String type = obj.path("Type").asText();
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
