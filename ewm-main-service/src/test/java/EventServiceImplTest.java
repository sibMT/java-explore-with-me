import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.main.category.repository.CategoryRepository;
import ru.practicum.ewm.main.event.EventMapper;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.event.service.EventServiceImpl;
import ru.practicum.ewm.main.exception.BadRequestException;
import ru.practicum.ewm.main.user.repository.UserRepository;
import ru.practicum.ewm.stats.client.StatsClient;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StatsClient statsClient;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void getPublicEvents_RangeInvalid() {
        LocalDateTime start = LocalDateTime.now().plusDays(5);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        assertThrows(BadRequestException.class, () ->
                eventService.getPublicEvents(
                        "", null, null, start, end, false,
                        null, 0, 10, "ip", "/events"
                )
        );
    }
}

