package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.CategoryRequest;
import org.example.dto.response.CategoryResponse;
import org.example.entity.Category;
import org.example.exception.InvalidRequestException;
import org.example.mapper.CategoryMapper;
import org.example.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @PostMapping
    public CategoryResponse createCategory(
            @RequestBody CategoryRequest categoryRequest
    ) {
        if (categoryRequest == null) {
            throw new InvalidRequestException("Category request is null");
        }
        Category entity = categoryMapper.toEntity(categoryRequest);
        Category category = categoryService.createCategory(entity);
        return categoryMapper.toResponse(category);
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategoryById(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Id is null");
        }
        Category categoryById = categoryService.getCategoryById(id);
        return categoryMapper.toResponse(categoryById);
    }

    @GetMapping
    public List<CategoryResponse> getAllCAtegories() {
        return categoryService.getAll()
                .stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public CategoryResponse updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequest categoryRequest
    ) {
        if (id == null) {
            throw new InvalidRequestException("Id is null");
        }
        Category updated = categoryService.updateCategory(id, categoryRequest);
        return categoryMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("Id is null");
        }
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
