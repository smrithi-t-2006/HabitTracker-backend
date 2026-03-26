package com.habitiq.service;

import com.habitiq.dto.HabitTemplateDto;
import com.habitiq.entity.Category;
import com.habitiq.entity.HabitTemplate;
import com.habitiq.exception.BadRequestException;
import com.habitiq.exception.ResourceNotFoundException;
import com.habitiq.repository.CategoryRepository;
import com.habitiq.repository.HabitTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HabitTemplateService {

    private final HabitTemplateRepository templateRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public HabitTemplateDto.Response createTemplate(HabitTemplateDto.CreateRequest request) {
        if (templateRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
            throw new BadRequestException("Template with this name already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        HabitTemplate template = HabitTemplate.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(category)
                .difficulty(request.getDifficulty())
                .frequency(request.getFrequency())
                .color(request.getColor())
                .build();

        template = templateRepository.save(template);
        return mapToResponse(template);
    }

    @Transactional
    public HabitTemplateDto.Response updateTemplate(Long id, HabitTemplateDto.UpdateRequest request) {
        HabitTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        if (request.getName() != null && !request.getName().equals(template.getName())) {
            if (templateRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
                throw new BadRequestException("Template with this name already exists");
            }
            template.setName(request.getName());
        }

        if (request.getDescription() != null) {
            template.setDescription(request.getDescription());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            template.setCategory(category);
        }
        if (request.getDifficulty() != null) {
            template.setDifficulty(request.getDifficulty());
        }
        if (request.getFrequency() != null) {
            template.setFrequency(request.getFrequency());
        }
        if (request.getColor() != null) {
            template.setColor(request.getColor());
        }
        if (request.getIsPopular() != null) {
            template.setIsPopular(request.getIsPopular());
        }

        template = templateRepository.save(template);
        return mapToResponse(template);
    }

    public HabitTemplateDto.Response getTemplateById(Long id) {
        HabitTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        return mapToResponse(template);
    }

    public List<HabitTemplateDto.Response> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<HabitTemplateDto.Response> getTemplatesByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        return templateRepository.findByCategory(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<HabitTemplateDto.Response> getPopularTemplates() {
        return templateRepository.findByIsPopularTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<HabitTemplateDto.Response> getMostUsedTemplates() {
        return templateRepository.findMostUsedTemplates().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTemplate(Long id) {
        HabitTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        templateRepository.delete(template);
    }

    @Transactional
    public HabitTemplateDto.Response incrementUsageCount(Long id) {
        HabitTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        template.setUsageCount(template.getUsageCount() + 1);
        template = templateRepository.save(template);
        return mapToResponse(template);
    }

    private HabitTemplateDto.Response mapToResponse(HabitTemplate template) {
        return new HabitTemplateDto.Response(
                template.getId(),
                template.getName(),
                template.getDescription(),
                template.getCategory().getId(),
                template.getCategory().getName(),
                template.getDifficulty(),
                template.getFrequency(),
                template.getColor(),
                template.getIsPopular(),
                template.getUsageCount(),
                template.getCreatedAt()
        );
    }
}
