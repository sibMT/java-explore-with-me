package ru.practicum.ewm.main.event.service;

import ru.practicum.ewm.main.event.EventState;
import ru.practicum.ewm.main.event.dto.*;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventShortDto> getPublicEvents(String text,
                                        List<Long> categories,
                                        Boolean paid,
                                        LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd,
                                        Boolean onlyAvailable,
                                        EventSort sort,
                                        int from,
                                        int size,
                                        String clientIp,
                                        String requestUri);

    List<EventFullDto> getAdminEvents(List<Long> users,
                                      List<EventState> states,
                                      List<Long> categories,
                                      LocalDateTime rangeStart,
                                      LocalDateTime rangeEnd,
                                      int from,
                                      int size);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest dto);

    EventFullDto getPublicEvent(Long eventId, String clientIp, String requestUri);

    EventFullDto createEvent(Long userId, NewEventDto dto);

    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto getUserEvent(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest dto);

}
