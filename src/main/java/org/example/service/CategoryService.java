package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.CategoryRequest;
import org.example.entity.Category;
import org.example.exception.EntityNotFoundException;
import org.example.exception.ExistEntityException;
import org.example.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    public List<Category> getAll() {
        List<Category> categories = categoryRepository.findAll();
        log.debug("Fetched {} category(ies)", categories.size());
        return categories;
    }

    public Category getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Category not found: ID=%d", id)
                ));
        log.debug("Fetched Category: ID={}, Name='{}'", id, category.getName());
        return category;
    }

    public Category createCategory(Category category) {
        try {
            Category saved = categoryRepository.save(category);
            log.info("Created Category: ID={}, Name='{}'", saved.getId(), saved.getName());
            return saved;
        } catch (DataIntegrityViolationException ex) {
            log.warn("Attempt to create duplicate Category with name: '{}'", category.getName());
            throw new ExistEntityException(
                    String.format("Category with name '%s' already exists.", category.getName())
            );
        }
    }

    public Category updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Category not found: ID=%d", id)
                ));

        if (request.getName() != null && !request.getName().equals(category.getName())) {
            category.setName(request.getName());
            log.debug("Updated name for Category ID={}: '{}'", id, request.getName());
        } else {
            log.debug("No changes detected for Category: ID={}", id);
            return category; // Нет изменений — возвращаем без сохранения (опционально можно сохранить, но логируем)
        }

        Category saved = categoryRepository.save(category);
        log.info("Successfully updated Category: ID={}, Name='{}'", id, saved.getName());
        return saved;
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Category not found: ID=%d", id)
                ));

        categoryRepository.delete(category);
        log.info("Deleted Category: ID={}, Name='{}'", id, category.getName());
    }
}
