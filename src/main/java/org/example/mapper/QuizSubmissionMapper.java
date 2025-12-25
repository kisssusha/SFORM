package org.example.mapper;

import org.example.dto.nested.QuizInfo;
import org.example.dto.request.QuizSubmissionRequest;
import org.example.dto.response.QuizSubmissionResponse;
import org.example.entity.Quiz;
import org.example.entity.QuizSubmission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {QuizMapper.class, UserMapper.class})
public interface QuizSubmissionMapper {

    @Mapping(target = "quiz", source = "quizId", qualifiedByName = "quizIdToQuiz")
    @Mapping(target = "student", source = "studentId", qualifiedByName = "studentIdToUser")
    QuizSubmission toEntity(QuizSubmissionRequest request);

    @Mapping(target = "quiz", source = "quiz", qualifiedByName = "quizToQuizInfo")
    @Mapping(target = "student", source = "student", qualifiedByName = "userToUserInfo")
    QuizSubmissionResponse toResponse(QuizSubmission quizSubmission);

    @Named("quizToQuizInfo")
    default QuizInfo quizToQuizInfo(Quiz quiz) {
        if (quiz == null) {
            return null;
        }
        QuizInfo quizInfo = new QuizInfo();
        quizInfo.setId(quiz.getId());
        quizInfo.setTitle(quiz.getTitle());
        return quizInfo;
    }

    @Named("quizIdToQuiz")
    default Quiz quizIdToQuiz(Long quizId) {
        if (quizId == null) {
            return null;
        }
        Quiz quiz = new Quiz();
        quiz.setId(quizId);
        return quiz;
    }
}
