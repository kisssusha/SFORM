package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.ModuleRequest;
import org.example.dto.response.ModuleResponse;
import org.example.entity.Module;
import org.example.exception.InvalidRequestException;
import org.example.mapper.ModuleMapper;
import org.example.service.ModuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {
    private final ModuleService moduleService;
    private final ModuleMapper moduleMapper;


    @PostMapping
    public ModuleResponse createModule(@RequestBody ModuleRequest moduleRequest) {
        if (moduleRequest == null) {
            throw new InvalidRequestException("Module request cannot be null");
        }
        Module entity = moduleMapper.toEntity(moduleRequest);
        Module module = moduleService.createModule(entity);
        return moduleMapper.toResponse(module);
    }

    @GetMapping
    public List<ModuleResponse> getAllModules() {
        return moduleService.getAllModules()
                .stream()
                .map(moduleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ModuleResponse getModuleById(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Module id cannot be null");
        }
        Module module = moduleService.getModuleById(id);
        return moduleMapper.toResponse(module);
    }

    @PutMapping("/{id}")
    public ModuleResponse updateModule(
            @PathVariable Long id,
            @RequestBody ModuleRequest moduleRequest
    ) {
        if (id == null || moduleRequest == null) {
            throw new InvalidRequestException("Module id or request cannot be null");
        }
        Module updatedModule = moduleService.updateModule(id, moduleRequest);
        return moduleMapper.toResponse(updatedModule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Module id cannot be null");
        }
        moduleService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }
}
