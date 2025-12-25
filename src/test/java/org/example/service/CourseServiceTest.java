package org.example.service;

import org.example.dto.request.CourseRequest;
import org.example.entity.Category;
import org.example.entity.Course;
import org.example.entity.Enrollment;
import org.example.entity.User;
import org.example.repository.CategoryRepository;
import org.example.repository.CourseRepository;
import org.example.repository.EnrollmentRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private CourseService courseService;

    /**
     * Должен вернуть список всех курсов при вызове getAll.
     */
    @Test
    public void shouldReturnAllCoursesWhenGetAllIsCalled() {
        // Given
        List<Course> courses = Arrays.asList(
                new Course(),
                new Course()
        );
        when(courseRepository.findAll()).thenReturn(courses);

        // When
        List<Course> result = courseService.getAll();

        // Then
        assertThat(result).hasSize(2);
        verify(courseRepository).findAll();
    }

    /**
     * Должен вернуть курс по ID, если он существует.
     */
    @Test
    public void shouldReturnCourseByIdWhenExists() {
        // Given
        Course course = new Course();
        course.setTitle("Java Fundamentals");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        // When
        Course found = courseService.getCourseById(1L);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("Java Fundamentals");
        verify(courseRepository).findById(1L);
    }

    /**
     * Должен вернуть список курсов, на которые записан пользователь.
     */
    @Test
    public void shouldReturnCoursesByUserIdWhenEnrollmentsExist() {
        // Given
        User user = new User();
        user.setId(1L);

        Course course1 = new Course();
        course1.setTitle("Web Development");

        Course course2 = new Course();
        course2.setTitle("Data Structures");

        Enrollment e1 = new Enrollment();
        e1.setUser(user);
        e1.setCourse(course1);

        Enrollment e2 = new Enrollment();
        e2.setUser(user);
        e2.setCourse(course2);

        when(enrollmentRepository.findByUserId(1L)).thenReturn(Arrays.asList(e1, e2));

        // When
        List<Course> courses = courseService.getCoursesByUserId(1L);

        // Then
        assertThat(courses).hasSize(2);
        assertThat(courses).extracting(Course::getTitle)
                .containsExactlyInAnyOrder("Web Development", "Data Structures");
        verify(enrollmentRepository).findByUserId(1L);
    }

    /**
     * Должен вернуть список студентов, записанных на курс.
     */
    @Test
    public void shouldReturnStudentsByCourseIdWhenEnrollmentsExist() {
        // Given
        User student1 = new User();
        student1.setName("Alice");
        student1.setEmail("alice@example.com");

        User student2 = new User();
        student2.setName("Bob");
        student2.setEmail("bob@example.com");

        Enrollment e1 = new Enrollment();
        e1.setUser(student1);

        Enrollment e2 = new Enrollment();
        e2.setUser(student2);

        when(enrollmentRepository.findByCourseId(1L)).thenReturn(Arrays.asList(e1, e2));

        // When
        List<User> students = courseService.getStudentsByCourseId(1L);

        // Then
        assertThat(students).hasSize(2);
        assertThat(students).extracting(User::getName)
                .containsExactlyInAnyOrder("Alice", "Bob");
        verify(enrollmentRepository).findByCourseId(1L);
    }

    /**
     * Должен создать курс, если преподаватель имеет корректную роль и категория существует.
     */
    @Test
    public void shouldCreateCourseWhenTeacherAndCategoryAreValid() {
        // Given
        User teacher = new User();
        teacher.setId(1L);
        teacher.setRole(User.Role.TEACHER);

        Category category = new Category();
        category.setId(2L);
        category.setName("Programming");

        Course course = new Course();
        course.setTitle("Advanced Spring Boot");
        course.setDescription("Master Spring Boot with real-world projects.");
        course.setTeacher(teacher);
        course.setCategory(category);

        when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(courseRepository.save(course)).thenReturn(course);

        // When
        Course created = courseService.createCourse(course);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getTitle()).isEqualTo("Advanced Spring Boot");
        assertThat(created.getTeacher().getId()).isEqualTo(1L);
        assertThat(created.getCategory().getId()).isEqualTo(2L);
        verify(userRepository).findById(1L);
        verify(categoryRepository).findById(2L);
        verify(courseRepository).save(course);
    }

    /**
     * Должен обновить все поля курса при корректном запросе.
     */
    @Test
    public void shouldUpdateCourseWhenValidRequestProvided() {
        // Given
        Course existing = new Course();
        existing.setId(1L);
        existing.setTitle("Old Course Title");
        existing.setDescription("Outdated description.");

        User oldTeacher = new User();
        oldTeacher.setId(1L);
        oldTeacher.setRole(User.Role.TEACHER);
        existing.setTeacher(oldTeacher);

        Category oldCategory = new Category();
        oldCategory.setId(1L);
        existing.setCategory(oldCategory);

        CourseRequest request = new CourseRequest();
        request.setTitle("Updated: Microservices with Spring Cloud");
        request.setDescription("Learn to build scalable microservices.");
        request.setTeacherId(2L);
        request.setCategoryId(3L);
        request.setStartDate(LocalDate.of(2025, 10, 1));
        request.setDuration(12);

        User newTeacher = new User();
        newTeacher.setId(2L);
        newTeacher.setRole(User.Role.TEACHER);

        Category newCategory = new Category();
        newCategory.setId(3L);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newTeacher));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(newCategory));
        when(courseRepository.save(existing)).thenReturn(existing);

        // When
        Course updated = courseService.updateCourse(1L, request);

        // Then
        assertThat(updated.getTitle()).isEqualTo("Updated: Microservices with Spring Cloud");
        assertThat(updated.getDescription()).isEqualTo("Learn to build scalable microservices.");
        assertThat(updated.getTeacher().getId()).isEqualTo(2L);
        assertThat(updated.getCategory().getId()).isEqualTo(3L);
        assertThat(updated.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(updated.getDuration()).isEqualTo(request.getDuration());
        verify(courseRepository).save(existing);
    }
}
