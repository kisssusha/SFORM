package org.example.mapper;

import org.example.dto.request.AnswerOptionRequest;
import org.example.dto.response.AnswerOptionResponse;
import org.example.entity.AnswerOption;
import org.example.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AnswerOptionMapper {

    @Mapping(target = "question", source = "questionId", qualifiedByName = "questionIdToQuestion")
    AnswerOption toEntity(AnswerOptionRequest request);

    AnswerOptionResponse toResponse(AnswerOption answerOption);

    @Named("questionIdToQuestion")
    default Question questionIdToQuestion(Long questionId) {
        if (questionId == null) {
            return null;
        }
        Question question = new Question();
        question.setId(questionId);
        return question;
    }
}
