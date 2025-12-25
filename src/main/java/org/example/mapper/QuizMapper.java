package org.example.mapper;

import org.example.dto.nested.ModuleInfo;
import org.example.dto.request.QuizRequest;
import org.example.dto.response.QuizResponse;
import org.example.entity.Module;
import org.example.entity.Quiz;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {ModuleMapper.class})
public interface QuizMapper {

    @Mapping(target = "module", source = "moduleId", qualifiedByName = "moduleIdToModule")
    Quiz toEntity(QuizRequest request);

    @Mapping(target = "module", source = "module", qualifiedByName = "moduleToModuleInfo")
    QuizResponse toResponse(Quiz quiz);

    @Named("moduleToModuleInfo")
    default ModuleInfo moduleToModuleInfo(Module module) {
        if (module == null) {
            return null;
        }
        ModuleInfo moduleInfo = new ModuleInfo();
        moduleInfo.setId(module.getId());
        moduleInfo.setTitle(module.getTitle());
        return moduleInfo;
    }

    @Named("moduleIdToModule")
    default Module moduleIdToModule(Long moduleId) {
        if (moduleId == null) {
            return null;
        }
        Module module = new Module();
        module.setId(moduleId);
        return module;
    }
}
