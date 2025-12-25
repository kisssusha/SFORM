package org.example.mapper;

import org.example.dto.nested.QuizInfo;
import org.example.dto.request.QuestionRequest;
import org.example.dto.response.QuestionResponse;
import org.example.entity.Question;
import org.example.entity.Quiz;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {QuizMapper.class})
public interface QuestionMapper {

    @Mapping(target = "quiz", source = "quizId", qualifiedByName = "quizIdToQuiz")
    @Mapping(target = "type", expression = "java(Question.QuestionType.valueOf(request.getType()))")
    Question toEntity(QuestionRequest request);

    @Mapping(target = "quiz", source = "quiz", qualifiedByName = "quizToQuizInfo")
    @Mapping(target = "type", expression = "java(question.getType().name())")
    QuestionResponse toResponse(Question question);

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
