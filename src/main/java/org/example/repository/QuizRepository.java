package org.example.repository;

import org.example.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByModule_CourseId(Long courseId);
    List<Quiz> findByModuleId(Long moduleId);

}
