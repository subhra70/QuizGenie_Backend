package com.subhrashaw.QuizGeneratorBackend.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GenerationService {

    private static final Logger logger = LoggerFactory.getLogger(GenerationService.class);

    @Value("${perplexity.api.key}")
    private String apiKey;

    @Value("${perplexity.api.url}")
    private String apiUrl;

    @Autowired
    private QuizQuestionsRepo quizQuestionsRepo;

    private final QuizService quizService;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    @Autowired
    public GenerationService(QuizService quizService, WebClient.Builder webClientBuilder) {
        this.quizService = quizService;
        this.objectMapper = new ObjectMapper();
        this.webClient = webClientBuilder.build();
    }

    public GenerateResponse generate(QuizRequest request) {
        String query = getQuery(request);
        if (query == null) {
            logger.warn("Unsupported quiz format: {}", request.getFormat());
            return null;
        }

        logger.info("Generated query for AI model:\n{}", query);

        try {
            String safeQuery = query.replace("\"", "\\\"");
            Map<String, Object> body = Map.of(
                    "model", "sonar-pro",
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are a helpful quiz generation assistant."),
                            Map.of("role", "user", "content", query)
                    ),
                    "max_tokens", 8000,
                    "temperature", 0.5
            );

            String responseText= webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (responseText == null || responseText.isEmpty()) {
                logger.warn("Empty response from Perplexity API");
                return null;
            }

            JsonNode rootNode=objectMapper.readTree(responseText);
            JsonNode contentNode=null;
            if (rootNode.has("choices") && rootNode.get("choices").isArray()) {
                JsonNode choices = rootNode.get("choices");
                if (!choices.isEmpty()) {
                    JsonNode messageNode = choices.get(0).path("message");
                    contentNode = messageNode.path("content");
                }
            }
            if (contentNode != null && contentNode.isTextual()) {
                String content = contentNode.asText();
                content = cleanResponse(content); // use your existing cleanResponse() method
                System.out.println(content);
                rootNode = objectMapper.readTree(content); // now this should be an array
            } else {
                logger.warn("Could not extract valid JSON content from API response");
                return null;
            }

            GenerateResponse generateResponse = new GenerateResponse();
            List<QuizQuestion> questions = new ArrayList<>();
            int totalMarks = 0;
            for (JsonNode node : rootNode) {
                QuizQuestion q = createQuizQuestion(node, request.getFormat());
                System.out.println(q.toString());
                if (q != null) {
                    quizQuestionsRepo.save(q);
                    questions.add(q);
                    totalMarks += q.getMarks().getMark();
                }
            }
            generateResponse.setQuestions(questions);
            generateResponse.setFullMarks(totalMarks);
            return generateResponse;

        } catch (Exception e) {
            logger.error("Error during quiz generation", e);
            return null;
        }
    }

    private String getQuery(QuizRequest request) {
        return switch (request.getFormat()) {
            case "GATE" -> String.format(
                    "Generate questions for %s examination consists of exactly %d questions of total 100 marks. " +
                            "First 10 questions (5 MCQ 1 mark, 3 MCQ 2 marks, and 2 MSQ 2 marks) must be from General Aptitude (verbal and quantitative aptitude) and appear consecutively. " +
                            "The remaining 55 questions (85 marks) are from the subject paper and comprise MCQ(Multiple Choice Question), MSQ(Multiple Select Questions), and NAT(Numerical Answer Types) types. " +
                            "Do not place all questions from the same subject consecutively. " +
                            " Return JSON friendly response where keys are exactly: 'Question', 'Options'(comma separated not in array), 'Answers'(comma separated for MSQ not in array), 'Type', 'Marks'. " +
                            "Additional details: %s\nIf difficulty not mentioned then maintain hard difficulty. When difficulty is hard then for engineering subjects 30/55 must be numerical based (if possible).",
                    request.getFormat(), request.getTotalQuestion(), request.getDescription());

            case "NIMCET" -> String.format(
                    "Generate a question set for %s with exactly %d MCQ-type questions. " +
                            "50 questions from Mathematics of 12 marks each, 40 questions from Analytical & Logical Reasoning of 6 marks each, " +
                            "20 questions from Computer Awareness 6 marks each, and 10 questions from English of 4 marks each. Questions type may follow the pattern and question type like the paper of year 2024." +
                            " Return JSON friendly response where keys are exactly: 'Question', 'Options'(only comma separated not in array), 'Answers'(only comma separated for MSQ not in array), 'Type', 'Marks'. " +
                            "Additional details: %s",
                    request.getFormat(), request.getTotalQuestion(), request.getDescription());

            case "JECA" -> String.format(
                    "Generate a question set for %s exam with exactly %d questions: 80 MCQs (1 mark each) followed by 20 MSQs (2 marks each). " +
                            "Do not place all questions from the same subject consecutively. " +
                            " Return JSON friendly response where keys are exactly: 'Question', 'Options'(comma separated not in array), 'Answers'(comma separated for MSQ not in array), 'Type', 'Marks'. " +
                            "Additional details: %s\n If difficulty not mentioned maintain hard level difficulty. For hard level at least 30 question must be numerical based. For medium difficulty at least 20 questions must be numerical based.",
                    request.getFormat(), request.getTotalQuestion(), request.getDescription());

            case "CUET(UG)" -> String.format(
                    "Generate %d questions for CUET(UG): 50 English Questions (RC, Vocabulary, Grammar), " +
                            "50 Questions Subject based (Class 12 syllabus), 50 questions from GK/Current Affairs/Reasoning. " +
                            "All are single-answer MCQs (5 marks each).  Return JSON friendly response where keys are exactly: 'Question', 'Options'(comma separated not in array), 'Answers'(comma separated for MSQ not in array), 'Type', 'Marks'. " +
                            "Additional details: %s",
                    request.getTotalQuestion(), request.getDescription());

            case "CUET(PG)" -> String.format(
                    "Generate %d questions for CUET(PG), Each of type MCQ 4 marks each. Make sure at least one questions must come from each of the topic mentioned in the syllabus. Maintain the difficulty mentioned in additional details (Difficulty Hard if not mentioned)." +
                            " Return JSON friendly response where keys are exactly: 'Question', 'Options'(comma separated not in array), 'Answers'(comma separated for MSQ not in array), 'Type', 'Marks'. Additional Details: %s",
                    request.getTotalQuestion(), request.getDescription());

            case "Other" -> String.format(
                    "Generate exactly %d questions: %d MCQ type questions of mark 1,%d MCQ type questions of mark 2, %d MSQ type questions of mark 1, %d MSQ type question of mark 2, %d NAT(Numerical Answer Types) type question of mark 1 and %d NAT(Numerical Answer Types) type question of mark 2." +
                            "Ensure subject and type (For multiple subjects or multiple type questions) shuffling. Return JSON friendly response where keys are exactly: 'Question', 'Options'(comma separated not in array), 'Answers'(comma separated for MSQ not in array), 'Type', 'Marks'. " +
                            "For NAT type, skip options. Additional details: %s",
                    request.getTotalQuestion(), request.getMcq1(), request.getMcq2(), request.getMsq1(), request.getMsq2(),
                    request.getNat1(), request.getNat2(), request.getDescription());

            default -> null;
        };
    }

    private QuizQuestion createQuizQuestion(JsonNode obj, String format) {
        try {
            String question = obj.path("Question").asText("");
            String optionsStr = obj.path("Options").asText("");
            String[] options = optionsStr.isEmpty() ? new String[0] : optionsStr.split(",");
            String type = obj.path("Type").asText("").toUpperCase();
            int marks = obj.path("Marks").asInt(0);
            String answer = obj.path("Answers").asText("");
            if (question.isEmpty() || type.isEmpty() || answer.isEmpty()) {
                logger.warn("Skipping malformed question: {}", question);
                return null;
            }

            QuizMarks qmarks = quizService.getQuizMarks(type, marks, format);
            boolean isOptionBased = type.equalsIgnoreCase("MCQ") || type.equalsIgnoreCase("MSQ");
            if (isOptionBased && options.length < 4) {
                logger.warn("Skipping question '{}' due to insufficient options ({})", question, options.length);
                return null;
            }

            return new QuizQuestion(
                    question,
                    (options.length > 0) ? options[0].trim() : "",
                    (options.length > 1) ? options[1].trim() : "",
                    (options.length > 2) ? options[2].trim() : "",
                    (options.length > 3) ? options[3].trim() : "",
                    answer,
                    qmarks
            );

        } catch (Exception e) {
            logger.error("Error parsing quiz question: {}", obj.toString(), e);
            return null;
        }
    }
    private String cleanResponse(String response) {
        if (response == null || response.isEmpty()) {
            return "";
        }

        // Remove Markdown-style code block markers like ```json or ```
        response = response.replaceAll("(?s)```json", ""); // removes starting ```json
        response = response.replaceAll("(?s)```", "");     // removes ending ```

        // Trim leading/trailing spaces and newlines
        response = response.trim();

        // Sometimes the response includes unwanted text before/after JSON
        // Try to extract only the JSON array/object part
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');
        if (start != -1 && end != -1 && end > start) {
            response = response.substring(start, end + 1);
        } else {
            start = response.indexOf('{');
            end = response.lastIndexOf('}');
            if (start != -1 && end != -1 && end > start) {
                response = response.substring(start, end + 1);
            }
        }

        // Remove extra escape characters
        response = response.replace("\\n", "")
                .replace("\\\"", "\"")
                .replace("\\t", "")
                .replace("\\r", "")
                .trim();

        return response;
    }

}
