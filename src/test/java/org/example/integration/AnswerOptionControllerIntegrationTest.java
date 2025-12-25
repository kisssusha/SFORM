package org.example.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.request.AnswerOptionRequest;
import org.example.entity.AnswerOption;
import org.example.entity.Question;
import org.example.entity.Quiz;
import org.example.repository.AnswerOptionRepository;
import org.example.repository.QuestionRepository;
import org.example.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты для AnswerOptionController.
 * Проверяют CRUD-операции для вариантов ответов через HTTP API.
 * Использует Testcontainers для запуска реальной PostgreSQL.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AnswerOptionControllerIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.5")
            .withDatabaseName(System.getenv().getOrDefault("DB_NAME", "testdb"))
            .withUsername(System.getenv().getOrDefault("DB_USERNAME", "testuser"))
            .withPassword(System.getenv().getOrDefault("DB_PASSWORD", "testpass"));
    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnswerOptionRepository answerOptionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    private Question question;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    public void setup() {
        // Создаём тестовый квиз
        Quiz quiz = new Quiz();
        quiz.setTitle("Quiz 1");
        quiz = quizRepository.save(quiz);

        // Создаём тестовый вопрос
        question = new Question();
        question.setText("Question 1");
        question.setType(Question.QuestionType.SINGLE_CHOICE);
        question.setQuiz(quiz);
        question = questionRepository.save(question);
    }

    /**
     * Должен вернуть 200 OK и список всех вариантов ответов.
     */
    @Test
    public void shouldReturnAllAnswerOptions() throws Exception {
        AnswerOption option1 = new AnswerOption();
        option1.setText("True");
        option1.setIsCorrect(true);
        option1.setQuestion(question);

        AnswerOption option2 = new AnswerOption();
        option2.setText("False");
        option2.setIsCorrect(false);
        option2.setQuestion(question);

        answerOptionRepository.saveAll(List.of(option1, option2));

        mockMvc.perform(get("/api/answer-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].text", hasItem("True")))
                .andExpect(jsonPath("$[*].text", hasItem("False")));
    }

    /**
     * Должен обновить вариант ответа и вернуть 200 OK с обновлёнными данными.
     */
    @Test
    public void shouldUpdateAnswerOptionAndReturnUpdatedData() throws Exception {
        AnswerOption option = new AnswerOption();
        option.setText("Old Answer");
        option.setIsCorrect(false);
        option.setQuestion(question);
        option = answerOptionRepository.save(option);

        AnswerOptionRequest request = new AnswerOptionRequest();
        request.setText("Updated Answer");
        request.setIsCorrect(true);
        request.setQuestionId(question.getId());

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/answer-options/{id}", option.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated Answer"))
                .andExpect(jsonPath("$.isCorrect").value(true));
    }

    /**
     * Должен удалить вариант ответа и вернуть 204 No Content.
     * При последующем запросе должен вернуть 404 Not Found.
     */
    @Test
    public void shouldDeleteAnswerOptionAndReturnNoContent() throws Exception {
        AnswerOption option = new AnswerOption();
        option.setText("To Be Deleted");
        option.setIsCorrect(false);
        option.setQuestion(question);
        option = answerOptionRepository.save(option);

        // Удаление
        mockMvc.perform(delete("/api/answer-options/{id}", option.getId()))
                .andExpect(status().isNoContent());

        // Проверка, что больше не существует
        mockMvc.perform(get("/api/answer-options/{id}", option.getId()))
                .andExpect(status().isNotFound());
    }
}
