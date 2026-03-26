package com.habitiq.controller;

import com.habitiq.dto.BadgeDto;
import com.habitiq.dto.UserBadgeDto;
import com.habitiq.entity.Badge.BadgeCategory;
import com.habitiq.service.BadgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
public class BadgeController {
    @Autowired private BadgeService badgeService;

    @GetMapping
    public ResponseEntity<List<BadgeDto.Response>> getAllBadges() {
        return ResponseEntity.ok(badgeService.getAllBadges());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<BadgeDto.Response>> getBadgesByCategory(@PathVariable BadgeCategory category) {
        return ResponseEntity.ok(badgeService.getBadgesByCategory(category));
    }

    @PostMapping
    public ResponseEntity<BadgeDto.Response> createBadge(@RequestBody BadgeDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(badgeService.createBadge(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BadgeDto.Response> updateBadge(@PathVariable Long id, @RequestBody BadgeDto.UpdateRequest request) {
        return ResponseEntity.ok(badgeService.updateBadge(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBadge(@PathVariable Long id) {
        badgeService.deleteBadge(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/badges")
    public ResponseEntity<List<UserBadgeDto.Response>> getUserBadges() {
        return ResponseEntity.ok(badgeService.getUserBadges());
    }

    @GetMapping("/user/earned")
    public ResponseEntity<List<UserBadgeDto.Response>> getUserEarnedBadges() {
        return ResponseEntity.ok(badgeService.getUserEarnedBadges());
    }

    @GetMapping("/user/count")
    public ResponseEntity<Integer> getEarnedBadgeCount() {
        return ResponseEntity.ok(badgeService.getUserEarnedBadgeCount());
    }
}
