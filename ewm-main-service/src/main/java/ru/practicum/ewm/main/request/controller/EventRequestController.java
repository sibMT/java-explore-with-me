package ru.practicum.ewm.main.request.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.main.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.main.request.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events/{eventId}/requests")
public class EventRequestController {

    private final RequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId,
                                                          @PathVariable Long eventId) {
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping
    public EventRequestStatusUpdateResult updateEventRequests(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @Valid @RequestBody EventRequestStatusUpdateRequest dto) {
        return requestService.updateEventRequests(userId, eventId, dto);
    }

}
