package org.example.integration;

import org.example.entity.*;
import org.example.entity.Module;
import org.example.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционный тест для проверки инициализации базы данных.
 * Проверяет, что все сущности были корректно загружены через Liquibase
 * при старте приложения (например, из файлов data.sql или changesets).
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class DBInitializationTest {

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

    @Autowired private CourseRepository courseRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ModuleRepository moduleRepository;
    @Autowired private LessonRepository lessonRepository;
    @Autowired private QuizRepository quizRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private AnswerOptionRepository answerOptionRepository;
    @Autowired private AssignmentRepository assignmentRepository;
    @Autowired private SubmissionRepository submissionRepository;
    @Autowired private QuizSubmissionRepository quizSubmissionRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;
    @Autowired private ProfileRepository profileRepository;

    /**
     * Должен содержать предзагруженные данные во всех основных таблицах,
     * загруженные через Liquibase при старте приложения.
     * Проверяет наличие записей в:
     * - Категории, Пользователи, Курсы
     * - Модули, Уроки, Задания
     * - Викторины, Вопросы, Варианты ответов
     * - Решения (сабмиты), Результаты викторин
     * - Теги, Записи на курсы, Профили
     */
    @Test
    public void shouldHaveAllRequiredDataPreloadedByLiquibase() {
        // Проверка: Категории
        List<Category> categories = categoryRepository.findAll();
        assertThat(categories)
                .as("Категории должны быть загружены (например, 'Programming', 'Data Science')")
                .isNotEmpty();

        // Проверка: Пользователи
        List<User> users = userRepository.findAll();
        assertThat(users)
                .as("Пользователи (преподаватели и студенты) должны быть предзагружены")
                .isNotEmpty();

        // Проверка: Курсы
        List<Course> courses = courseRepository.findAll();
        assertThat(courses)
                .as("Курсы (например, 'Java Basics', 'Web Development') должны быть загружены")
                .isNotEmpty();

        // Проверка: Модули
        List<Module> modules = moduleRepository.findAll();
        assertThat(modules)
                .as("Модули должны быть привязаны к курсам и загружены")
                .isNotEmpty();

        // Проверка: Уроки
        List<Lesson> lessons = lessonRepository.findAll();
        assertThat(lessons)
                .as("Уроки должны быть загружены и привязаны к модулям")
                .isNotEmpty();

        // Проверка: Задания
        List<Assignment> assignments = assignmentRepository.findAll();
        assertThat(assignments)
                .as("Задания должны быть загружены для практических работ")
                .isNotEmpty();

        // Проверка: Викторины
        List<Quiz> quizzes = quizRepository.findAll();
        assertThat(quizzes)
                .as("Викторины должны быть загружены для модулей")
                .isNotEmpty();

        // Проверка: Вопросы
        List<Question> questions = questionRepository.findAll();
        assertThat(questions)
                .as("Вопросы должны быть загружены для викторин")
                .isNotEmpty();

        // Проверка: Варианты ответов
        List<AnswerOption> options = answerOptionRepository.findAll();
        assertThat(options)
                .as("Варианты ответов (для вопросов) должны быть загружены")
                .isNotEmpty();

        // Проверка: Решения студентов (сабмиты заданий)
        List<Submission> submissions = submissionRepository.findAll();
        assertThat(submissions)
                .as("Решения студентов по заданиям должны быть загружены")
                .isNotEmpty();

        // Проверка: Результаты прохождения викторин
        List<QuizSubmission> quizSubmissions = quizSubmissionRepository.findAll();
        assertThat(quizSubmissions)
                .as("Результаты прохождения викторин должны быть загружены")
                .isNotEmpty();

        // Проверка: Теги (для категоризации курсов/уроков)
        List<Tag> tags = tagRepository.findAll();
        assertThat(tags)
                .as("Теги (например, 'Spring', 'REST', 'Security') должны быть загружены")
                .isNotEmpty();

        // Проверка: Записи студентов на курсы
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        assertThat(enrollments)
                .as("Записи студентов на курсы должны быть загружены")
                .isNotEmpty();

        // Проверка: Профили пользователей
        List<Profile> profiles = profileRepository.findAll();
        assertThat(profiles)
                .as("Профили пользователей (био, аватарки) должны быть загружены")
                .isNotEmpty();

        // Опционально: логируем количество записей для отладки
        System.out.println("Preloaded data counts:");
        System.out.println("  Categories: " + categories.size());
        System.out.println("  Users: " + users.size());
        System.out.println("  Courses: " + courses.size());
        System.out.println("  Modules: " + modules.size());
        System.out.println("  Lessons: " + lessons.size());
        System.out.println("  Assignments: " + assignments.size());
        System.out.println("  Quizzes: " + quizzes.size());
        System.out.println("  Questions: " + questions.size());
        System.out.println("  AnswerOptions: " + options.size());
        System.out.println("  Submissions: " + submissions.size());
        System.out.println("  QuizSubmissions: " + quizSubmissions.size());
        System.out.println("  Tags: " + tags.size());
        System.out.println("  Enrollments: " + enrollments.size());
        System.out.println("  Profiles: " + profiles.size());
    }
}
