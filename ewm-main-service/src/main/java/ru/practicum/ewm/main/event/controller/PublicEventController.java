package ru.practicum.ewm.main.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.event.dto.EventFullDto;
import ru.practicum.ewm.main.event.dto.EventShortDto;
import ru.practicum.ewm.main.event.dto.EventSort;
import ru.practicum.ewm.main.event.service.EventService;
import ru.practicum.ewm.main.exception.BadRequestException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@Validated
public class PublicEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false) EventSort sort,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "10") @Min(1) Integer size,
            HttpServletRequest request
    ) {

        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("rangeEnd must be after rangeStart");
        }

        return eventService.getPublicEvents(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size,
                request.getRemoteAddr(),
                request.getRequestURI()
        );
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable Long eventId,
                                     HttpServletRequest request) {
        return eventService.getPublicEvent(
                eventId,
                request.getRemoteAddr(),
                request.getRequestURI()
        );
    }
}

