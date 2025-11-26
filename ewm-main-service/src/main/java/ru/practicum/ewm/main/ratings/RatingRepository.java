package ru.practicum.ewm.main.ratings;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByUserIdAndEventId(Long userId, Long eventId);

    long countByEventId(Long eventId);

    long countByEventIdAndIsLikeTrue(Long eventId);
}
