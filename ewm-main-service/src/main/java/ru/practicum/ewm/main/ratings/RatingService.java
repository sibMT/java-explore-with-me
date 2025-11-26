package ru.practicum.ewm.main.ratings;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.event.Event;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.exception.BadRequestException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.user.model.User;
import ru.practicum.ewm.main.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Transactional
    public String addRating(Long userId, Long eventId, boolean isLike) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new BadRequestException("Cannot rate own event");
        }

        Rating rating = ratingRepository.findByUserIdAndEventId(userId, eventId).orElse(null);

        if (rating != null) {
            if (rating.getIsLike() == isLike) {
                return "Rating unchanged";
            }
            rating.setIsLike(isLike);
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
            rating = Rating.builder()
                    .user(user)
                    .event(event)
                    .isLike(isLike)
                    .build();
        }

        ratingRepository.save(rating);

        return "Rating saved";
    }


    public double getEventRating(Long eventId) {
        long total = ratingRepository.countByEventId(eventId);
        long likes = ratingRepository.countByEventIdAndIsLikeTrue(eventId);
        return total > 0 ? (likes * 100.0) / total : 0.0;
    }

    public double getUserRating(Long userId) {
        List<Event> events = eventRepository.findByInitiatorId(userId, Pageable.unpaged()).getContent();
        if (events.isEmpty()) return 0.0;
        double sum = 0.0;
        int count = 0;
        for (Event event : events) {
            long total = ratingRepository.countByEventId(event.getId());
            long likes = ratingRepository.countByEventIdAndIsLikeTrue(event.getId());
            if (total > 0) {
                sum += (likes * 100.0) / total;
                count++;
            }
        }
        return count > 0 ? sum / count : 0.0;
    }
}
