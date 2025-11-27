package ru.practicum.ewm.main.ratings;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.main.event.Event;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByUserIdAndEventId(Long userId, Long eventId);

    long countByEventId(Long eventId);

    long countByEventIdAndIsLikeTrue(Long eventId);

    List<Rating> findAllByEventIn(List<Event> events);
}
