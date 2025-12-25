package org.example.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.request.CategoryRequest;
import org.example.entity.Category;
import org.example.repository.CategoryRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты для CategoryController.
 * Проверяют CRUD-операции для категорий через HTTP API.
 * Использует Testcontainers для запуска реальной PostgreSQL.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CategoryControllerIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.5")
            .withDatabaseName(System.getenv().getOrDefault("DB_NAME", "testdb"))
            .withUsername(System.getenv().getOrDefault("DB_USERNAME", "testuser"))
            .withPassword(System.getenv().getOrDefault("DB_PASSWORD", "testpass"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category existingCategory;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    public void setup() {
        categoryRepository.deleteAll(); // Очистка перед каждым тестом

        // Создаём базовую категорию
        existingCategory = new Category();
        existingCategory.setName("Programming");
        existingCategory = categoryRepository.save(existingCategory);
    }

    /**
     * Должен вернуть список всех категорий (минимум одна) при GET /api/categories.
     */
    @Test
    public void shouldReturnAllCategoriesWhenGetAllIsCalled() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].name").value("Programming"))
                .andExpect(jsonPath("$[0].id").value(existingCategory.getId()));
    }

    /**
     * Должен вернуть конкретную категорию по ID при GET /api/categories/{id}.
     */
    @Test
    public void shouldReturnCategoryByIdWhenExists() throws Exception {
        mockMvc.perform(get("/api/categories/{id}", existingCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingCategory.getId()))
                .andExpect(jsonPath("$.name").value("Programming"));
    }

    /**
     * Должен создать новую категорию и вернуть 200 OK с данными при POST /api/categories.
     */
    @Test
    public void shouldCreateNewCategoryAndReturnCreatedData() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Web Development");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Web Development"));

        // Проверка, что действительно сохранилось в БД
        long count = categoryRepository.count();
        org.assertj.core.api.Assertions.assertThat(count).isEqualTo(2);
    }

    /**
     * Должен обновить имя категории и вернуть 200 OK с обновлёнными данными при PUT /api/categories/{id}.
     */
    @Test
    public void shouldUpdateCategoryNameAndReturnUpdatedData() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Advanced Programming");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/categories/{id}", existingCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingCategory.getId()))
                .andExpect(jsonPath("$.name").value("Advanced Programming"));
    }

    /**
     * Должен удалить категорию и вернуть 204 No Content при DELETE /api/categories/{id}.
     * Последующий запрос должен вернуть 404 Not Found.
     */
    @Test
    public void shouldDeleteCategoryAndReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}", existingCategory.getId()))
                .andExpect(status().isNoContent());

        // Проверка, что категория больше не доступна
        mockMvc.perform(get("/api/categories/{id}", existingCategory.getId()))
                .andExpect(status().isNotFound());
    }
}
