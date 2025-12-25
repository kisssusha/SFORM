package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.ModuleRequest;
import org.example.entity.Course;
import org.example.entity.Module;
import org.example.exception.EntityNotFoundException;
import org.example.repository.CourseRepository;
import org.example.repository.ModuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private static final Logger log = LoggerFactory.getLogger(ModuleService.class);

    private final ModuleRepository moduleRepository;
    private final CourseRepository courseRepository;

    public List<Module> getAllModules() {
        List<Module> modules = moduleRepository.findAll();
        log.debug("Fetched {} module(s)", modules.size());
        return modules;
    }

    public Module getModuleById(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Module not found: ID=%d", id)
                ));
        log.debug("Fetched Module: ID={}, Title='{}', CourseID={}",
                id, module.getTitle(), module.getCourse().getId());
        return module;
    }

    public Module createModule(Module module) {
        Long courseId = module.getCourse().getId();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Course not found: ID=%d", courseId)
                ));

        module.setCourse(course);
        Module saved = moduleRepository.save(module);

        log.info("Created Module: ID={}, Title='{}', OrderIndex={}, CourseID={}",
                saved.getId(), saved.getTitle(), saved.getOrderIndex(), courseId);
        return saved;
    }

    public Module updateModule(Long id, ModuleRequest request) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Module not found: ID=%d", id)
                ));

        boolean updated = false;

        if (request.getTitle() != null && !request.getTitle().equals(module.getTitle())) {
            module.setTitle(request.getTitle());
            log.debug("Updated title for Module ID={}: '{}'", id, request.getTitle());
            updated = true;
        }

        if (request.getOrderIndex() != null && !request.getOrderIndex().equals(module.getOrderIndex())) {
            module.setOrderIndex(request.getOrderIndex());
            log.debug("Updated orderIndex for Module ID={}: {}", id, request.getOrderIndex());
            updated = true;
        }

        if (request.getCourseId() != null && !request.getCourseId().equals(module.getCourse().getId())) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Course not found: ID=%d", request.getCourseId())
                    ));
            module.setCourse(course);
            log.debug("Updated course for Module ID={}: CourseID={}", id, request.getCourseId());
            updated = true;
        }

        if (!updated) {
            log.debug("No changes detected for Module: ID={}", id);
            return module;
        }

        Module saved = moduleRepository.save(module);
        log.info("Successfully updated Module: ID={}, Title='{}', CourseID={}",
                id, saved.getTitle(), saved.getCourse().getId());
        return saved;
    }

    public void deleteModule(Long id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Module not found: ID=%d", id)
                ));

        moduleRepository.delete(module);

        log.info("Deleted Module: ID={}, Title='{}', CourseID={}",
                id, module.getTitle(), module.getCourse().getId());
    }
}
