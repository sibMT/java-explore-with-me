package ru.practicum.ewm.main.ratings;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PutMapping("/users/{userId}/events/{eventId}/like")
    public ResponseEntity<Map<String, Object>> addLike(@PathVariable Long userId,
                                                       @PathVariable Long eventId) {
        String result = ratingService.addRating(userId, eventId, true);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PutMapping("/users/{userId}/events/{eventId}/dislike")
    public ResponseEntity<Map<String, Object>> addDislike(@PathVariable Long userId,
                                                          @PathVariable Long eventId) {
        String result = ratingService.addRating(userId, eventId, false);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @GetMapping("/users/{userId}/rating")
    public ResponseEntity<Double> getUserRating(@PathVariable Long userId) {
        double rating = ratingService.getUserRating(userId);
        return ResponseEntity.ok(rating);
    }

}
