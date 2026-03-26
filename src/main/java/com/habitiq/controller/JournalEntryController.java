package com.habitiq.controller;

import com.habitiq.dto.JournalEntryDto;
import com.habitiq.service.JournalEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/journal")
public class JournalEntryController {
    @Autowired private JournalEntryService journalEntryService;

    @GetMapping("/habit/{habitId}")
    public ResponseEntity<List<JournalEntryDto.Response>> getHabitEntries(@PathVariable Long habitId) {
        return ResponseEntity.ok(journalEntryService.getHabitEntries(habitId));
    }

    @GetMapping("/habit/{habitId}/date/{date}")
    public ResponseEntity<List<JournalEntryDto.Response>> getEntriesByDate(
            @PathVariable Long habitId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(journalEntryService.getEntriesByDate(habitId, date));
    }

    @GetMapping("/habit/{habitId}/range")
    public ResponseEntity<List<JournalEntryDto.Response>> getEntriesByDateRange(
            @PathVariable Long habitId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(journalEntryService.getEntriesByDateRange(habitId, startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<JournalEntryDto.Response> createEntry(@RequestBody JournalEntryDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(journalEntryService.createEntry(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JournalEntryDto.Response> updateEntry(@PathVariable Long id, @RequestBody JournalEntryDto.UpdateRequest request) {
        return ResponseEntity.ok(journalEntryService.updateEntry(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        journalEntryService.deleteEntry(id);
        return ResponseEntity.ok().build();
    }
}
