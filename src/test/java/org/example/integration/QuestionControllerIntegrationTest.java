package org.example.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.Question;
import org.example.entity.Quiz;
import org.example.repository.QuestionRepository;
import org.example.repository.QuizRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты для QuestionController.
 * Проверяют:
 * - создание вопроса для викторины
 * - удаление вопроса
 * Использует Testcontainers для запуска реальной PostgreSQL.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QuestionControllerIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.5")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    private Quiz quiz;

    @BeforeEach
    public void setup() {
        questionRepository.deleteAll();
        quizRepository.deleteAll();

        // Создаём тестовую викторину
        quiz = new Quiz();
        quiz.setTitle("Quiz 1: Java Basics");
        quiz = quizRepository.save(quiz);
    }

    @AfterEach
    public void tearDown() {
        questionRepository.deleteAll();
        quizRepository.deleteAll();
    }

    /**
     * Должен удалить вопрос и вернуть 204 No Content.
     * Последующий запрос должен вернуть 404 Not Found.
     */
    @Test
    @Order(2)
    public void shouldDeleteQuestionAndReturnNoContent() throws Exception {
        // Создаём вопрос напрямую в БД
        Question question = new Question();
        question.setText("Which keyword is used to inherit a class?");
        question.setType(Question.QuestionType.SINGLE_CHOICE);
        question.setQuiz(quiz);
        question = questionRepository.save(question);

        // Удаляем
        mockMvc.perform(delete("/api/questions/{id}", question.getId()))
                .andExpect(status().isNoContent());

        // Проверяем, что не существует
        mockMvc.perform(get("/api/questions/{id}", question.getId()))
                .andExpect(status().isNotFound());
    }
}
