import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.main.event.Event;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.ratings.Rating;
import ru.practicum.ewm.main.ratings.RatingRepository;
import ru.practicum.ewm.main.ratings.RatingService;
import ru.practicum.ewm.main.user.model.User;
import ru.practicum.ewm.main.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RatingServiceUnitTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private RatingService ratingService;

    private User user;
    private Event event;
    private Rating rating;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(2L);
        event = new Event();
        event.setId(1L);
        User owner = new User();
        owner.setId(1L);
        event.setInitiator(owner);
        rating = new Rating();
        rating.setId(10L);
        rating.setUser(user);
        rating.setEvent(event);
        rating.setIsLike(true);
    }

    @Test
    void addRating_NewLike() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(ratingRepository.findByUserIdAndEventId(2L, 1L)).thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        ratingService.addRating(2L, 1L, true);
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void addRating_UpdateExisting() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(ratingRepository.findByUserIdAndEventId(2L, 1L)).thenReturn(Optional.of(rating));
        ratingService.addRating(2L, 1L, false);
        assertFalse(rating.getIsLike());
        verify(ratingRepository).save(rating);
    }

    @Test
    void rate_OwnEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                ratingService.addRating(1L, 1L, true)
        );
        assertTrue(ex.getMessage().contains("Cannot rate own event"));
    }

    @Test
    void getEventRating_NoRatings() {
        when(ratingRepository.countByEventId(1L)).thenReturn(0L);
        when(ratingRepository.countByEventIdAndIsLikeTrue(1L)).thenReturn(0L);
        double result = ratingService.getEventRating(1L);
        assertEquals(0.0, result);
    }

    @Test
    void getEventRating_WithLikes() {
        when(ratingRepository.countByEventId(1L)).thenReturn(5L);
        when(ratingRepository.countByEventIdAndIsLikeTrue(1L)).thenReturn(2L);
        double result = ratingService.getEventRating(1L);
        assertEquals(40.0, result);
    }

    @Test
    void getEventRating_ShouldReturnPercentage() {
        when(ratingRepository.countByEventId(5L)).thenReturn(4L);
        when(ratingRepository.countByEventIdAndIsLikeTrue(5L)).thenReturn(3L);
        double rating = ratingService.getEventRating(5L);
        assertEquals(75.0, rating);
    }

}
