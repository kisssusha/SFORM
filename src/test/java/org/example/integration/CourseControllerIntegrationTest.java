package org.example.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.request.CourseRequest;
import org.example.dto.response.CourseResponse;
import org.example.entity.Category;
import org.example.entity.User;
import org.example.repository.CategoryRepository;
import org.example.repository.CourseRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты для CourseController.
 * Проверяют основные CRUD-операции и бизнес-логику:
 * - создание, удаление курса
 * - получение студентов по курсу
 * Использует Testcontainers для запуска реальной PostgreSQL.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourseControllerIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.5")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User teacher;
    private Category category;
    private CourseRequest baseCourseRequest;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    public void setup() {
        // Очистка репозиториев перед каждым тестом (если нужно)
        courseRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        // Создаём преподавателя
        teacher = new User();
        teacher.setName("Dr. Alice Smith");
        teacher.setEmail("alice.smith@university.edu");
        teacher.setRole(User.Role.TEACHER);
        teacher = userRepository.save(teacher);

        // Создаём категорию
        category = new Category();
        category.setName("Software Development");
        category = categoryRepository.save(category);

        // Базовый запрос для создания курса
        baseCourseRequest = new CourseRequest();
        baseCourseRequest.setTeacherId(teacher.getId());
        baseCourseRequest.setCategoryId(category.getId());
        baseCourseRequest.setStartDate(LocalDate.of(2025, 10, 1));
        baseCourseRequest.setDuration(12);
    }

    @AfterEach
    public void tearDown() {
        courseRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    /**
     * Должен создать курс и вернуть 200 OK с полными данными.
     */
    @Test
    @Order(1)
    public void shouldCreateCourseAndReturnCreatedData() throws Exception {
        baseCourseRequest.setTitle("Spring Boot Fundamentals");
        baseCourseRequest.setDescription("Learn Spring Boot from scratch: REST, Data, Security.");

        String json = objectMapper.writeValueAsString(baseCourseRequest);

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title").value("Spring Boot Fundamentals"))
                .andExpect(jsonPath("$.description").value("Learn Spring Boot from scratch: REST, Data, Security."))
                .andExpect(jsonPath("$.teacher.id").value(teacher.getId()))
                .andExpect(jsonPath("$.category.id").value(category.getId()))
                .andExpect(jsonPath("$.duration").value(12));

        // Проверка, что курс действительно сохранён
        long count = courseRepository.count();
        assertThat(count).isEqualTo(1);
    }

    /**
     * Должен удалить курс и вернуть 204 No Content.
     * Последующий запрос должен вернуть 404 Not Found.
     */
    @Test
    @Order(2)
    public void shouldDeleteCourseAndReturnNoContent() throws Exception {
        baseCourseRequest.setTitle("Course to Remove");
        baseCourseRequest.setDescription("This course will be deleted.");
        CourseResponse created = createCourse(baseCourseRequest);

        // Удаление
        mockMvc.perform(delete("/api/courses/{id}", created.getId()))
                .andExpect(status().isNoContent());

        // Проверка, что больше не существует
        mockMvc.perform(get("/api/courses/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }

    /**
     * Должен вернуть пустой список студентов, если никто не записан на курс.
     */
    @Test
    @Order(3)
    public void shouldReturnEmptyStudentsListWhenNoEnrollments() throws Exception {
        baseCourseRequest.setTitle("Advanced Java");
        baseCourseRequest.setDescription("Deep dive into JVM, concurrency, and performance.");
        CourseResponse created = createCourse(baseCourseRequest);

        mockMvc.perform(get("/api/courses/{courseId}/students", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }

    // Утилитарный метод для создания курса через API и получения ответа
    private CourseResponse createCourse(CourseRequest request) throws Exception {
        String json = objectMapper.writeValueAsString(request);
        String responseJson = mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(responseJson, CourseResponse.class);
    }
}