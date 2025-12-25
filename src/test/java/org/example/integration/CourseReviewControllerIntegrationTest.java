package org.example.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.request.CourseReviewRequest;
import org.example.entity.Course;
import org.example.entity.CourseReview;
import org.example.entity.User;
import org.example.repository.CourseRepository;
import org.example.repository.CourseReviewRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.*;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты для CourseReviewController.
 * Проверяют:
 * - создание отзыва через тело запроса
 * - создание отзыва через path variables (альтернативный эндпоинт)
 * - удаление отзыва
 * Использует Testcontainers для запуска реальной PostgreSQL.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourseReviewControllerIntegrationTest {

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
    private CourseReviewRepository courseReviewRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    private Course course;
    private User student;
    private User teacher;

    @BeforeEach
    public void setup() {
        courseReviewRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // Создаём преподавателя
        teacher = new User();
        teacher.setName("Dr. Robert Chen");
        teacher.setEmail("robert.chen@university.edu");
        teacher.setRole(User.Role.TEACHER);
        teacher = userRepository.save(teacher);

        // Создаём курс
        course = new Course();
        course.setTitle("Spring Boot Advanced");
        course.setDescription("Master Spring Boot with real-world projects and best practices.");
        course.setStartDate(LocalDate.now());
        course.setDuration(8);
        course.setTeacher(teacher);
        course = courseRepository.save(course);

        // Создаём студента
        student = new User();
        student.setName("Alice Johnson");
        student.setEmail("alice.johnson@student.edu");
        student.setRole(User.Role.STUDENT);
        student = userRepository.save(student);
    }

    @AfterEach
    public void tearDown() {
        courseReviewRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Должен создать отзыв, используя тело запроса с полным объектом,
     * и вернуть 200 OK с корректными данными.
     */
    @Test
    @Order(1)
    public void shouldCreateReviewWithRequestBodyAndReturnCreatedData() throws Exception {
        CourseReviewRequest request = new CourseReviewRequest();
        request.setCourseId(course.getId());
        request.setStudentId(student.getId());
        request.setRating(5);
        request.setComment("Outstanding course! Very well structured and informative.");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/course-reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Outstanding course! Very well structured and informative."))
                .andExpect(jsonPath("$.student.id").value(student.getId()))
                .andExpect(jsonPath("$.course.id").value(course.getId()));
    }

    /**
     * Должен создать отзыв, используя ID курса и студента из пути,
     * а данные отзыва — из тела запроса.
     */
    @Test
    @Order(2)
    public void shouldCreateReviewWithPathVariablesAndReturnCreatedData() throws Exception {
        CourseReviewRequest request = new CourseReviewRequest();
        request.setRating(4);
        request.setComment("Great content, but could use more exercises.");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/course-reviews/{courseId}/{studentId}", course.getId(), student.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Great content, but could use more exercises."))
                .andExpect(jsonPath("$.course.id").value(course.getId()))
                .andExpect(jsonPath("$.student.id").value(student.getId()));
    }

    /**
     * Должен удалить отзыв и вернуть 204 No Content.
     * Последующий запрос должен вернуть 404 Not Found.
     */
    @Test
    @Order(3)
    public void shouldDeleteReviewAndReturnNoContent() throws Exception {
        // Создаём отзыв напрямую в БД
        CourseReview review = new CourseReview();
        review.setCourse(course);
        review.setStudent(student);
        review.setRating(2);
        review.setComment("Too fast-paced for beginners.");
        review = courseReviewRepository.save(review);

        // Удаляем
        mockMvc.perform(delete("/api/course-reviews/{id}", review.getId()))
                .andExpect(status().isNoContent());

        // Проверяем, что не существует
        mockMvc.perform(get("/api/course-reviews/{id}", review.getId()))
                .andExpect(status().isNotFound());
    }
}
