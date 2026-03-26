package com.habitiq.controller;

import com.habitiq.dto.HabitTemplateDto;
import com.habitiq.service.HabitTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class HabitTemplateController {

    private final HabitTemplateService templateService;

    @PostMapping
    public ResponseEntity<HabitTemplateDto.Response> createTemplate(@Valid @RequestBody HabitTemplateDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.createTemplate(request));
    }

    @GetMapping
    public ResponseEntity<List<HabitTemplateDto.Response>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HabitTemplateDto.Response> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.getTemplateById(id));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<HabitTemplateDto.Response>> getTemplatesByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(templateService.getTemplatesByCategory(categoryId));
    }

    @GetMapping("/popular/list")
    public ResponseEntity<List<HabitTemplateDto.Response>> getPopularTemplates() {
        return ResponseEntity.ok(templateService.getPopularTemplates());
    }

    @GetMapping("/most-used/list")
    public ResponseEntity<List<HabitTemplateDto.Response>> getMostUsedTemplates() {
        return ResponseEntity.ok(templateService.getMostUsedTemplates());
    }

    @PutMapping("/{id}")
    public ResponseEntity<HabitTemplateDto.Response> updateTemplate(@PathVariable Long id,
                                                                     @Valid @RequestBody HabitTemplateDto.UpdateRequest request) {
        return ResponseEntity.ok(templateService.updateTemplate(id, request));
    }

    @PostMapping("/{id}/increment-usage")
    public ResponseEntity<HabitTemplateDto.Response> incrementUsageCount(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.incrementUsageCount(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
