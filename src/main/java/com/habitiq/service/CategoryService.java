package com.habitiq.service;

import com.habitiq.dto.CategoryDto;
import com.habitiq.entity.Category;
import com.habitiq.exception.BadRequestException;
import com.habitiq.exception.ResourceNotFoundException;
import com.habitiq.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryDto.Response createCategory(CategoryDto.CreateRequest request) {
        if (categoryRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
            throw new BadRequestException("Category with this name already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .color(request.getColor())
                .build();

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    @Transactional
    public CategoryDto.Response updateCategory(Long id, CategoryDto.UpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
                throw new BadRequestException("Category with this name already exists");
            }
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getColor() != null) {
            category.setColor(request.getColor());
        }

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    public CategoryDto.Response getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return mapToResponse(category);
    }

    public List<CategoryDto.Response> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        categoryRepository.delete(category);
    }

    private CategoryDto.Response mapToResponse(Category category) {
        return new CategoryDto.Response(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getIcon(),
                category.getColor(),
                category.getCreatedAt()
        );
    }
}
