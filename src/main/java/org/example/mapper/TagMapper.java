package org.example.mapper;

import org.example.dto.request.TagRequest;
import org.example.dto.response.TagResponse;
import org.example.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TagMapper {
    Tag toEntity(TagRequest request);
    TagResponse toResponse(Tag tag);
}
