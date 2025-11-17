package ru.practicum.ewm.main.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.event.Event;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.event.EventState;
import ru.practicum.ewm.main.exception.ConditionsNotMetException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.request.RequestMapper;
import ru.practicum.ewm.main.request.RequestRepository;
import ru.practicum.ewm.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.main.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.main.request.model.Request;
import ru.practicum.ewm.main.request.model.RequestStatus;
import ru.practicum.ewm.main.user.model.User;
import ru.practicum.ewm.main.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User with id=" + userId + " was not found") {
                });

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        "Event with id=" + eventId + " was not found") {
                });

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException(
                    "Initiator cannot request participation in own event") {
            };
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConditionsNotMetException(
                    "Cannot participate in an unpublished event") {
            };
        }
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConditionsNotMetException("Request already exists") {
            };
        }
        if (event.getParticipantLimit() > 0
                && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConditionsNotMetException("Participant limit has been reached") {
            };
        }

        Request request = new Request();
        request.setRequester(user);
        request.setEvent(event);
        request.setCreated(truncatedToMicros());

        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        Request saved = requestRepository.save(request);
        eventRepository.save(event);

        return requestMapper.toDto(saved);
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User with id=" + userId + " was not found") {
                });

        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(
                        "Request with id=" + requestId + " was not found") {
                });

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException(
                    "Request with id=" + requestId + " was not found") {
            };
        }

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            Event event = request.getEvent();
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
        }

        request.setStatus(RequestStatus.CANCELED);
        Request saved = requestRepository.save(request);
        return requestMapper.toDto(saved);
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = getEventForInitiator(userId, eventId);

        return requestRepository.findAllByEventId(event.getId()).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequests(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest requestUpdate) {

        Event event = getEventForInitiator(userId, eventId);
        if (requestUpdate.getStatus() != RequestStatus.CONFIRMED
                && requestUpdate.getStatus() != RequestStatus.REJECTED) {
            throw new ConditionsNotMetException("Status must be CONFIRMED or REJECTED") {
            };
        }

        List<Request> requests = requestRepository.findAllById(requestUpdate.getRequestIds());
        for (Request r : requests) {
            if (!r.getEvent().getId().equals(eventId)) {
                throw new ConditionsNotMetException("All requests must belong to the event") {
                };
            }
        }

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        int confirmedCount = event.getConfirmedRequests();
        int limit = event.getParticipantLimit();
        if (requestUpdate.getStatus() == RequestStatus.CONFIRMED
                && limit != 0
                && confirmedCount >= limit) {
            throw new ConditionsNotMetException("Participant limit has been reached") {
            };
        }

        for (Request r : requests) {
            if (r.getStatus() != RequestStatus.PENDING) {
                throw new ConditionsNotMetException("Only pending requests can be updated") {
                };
            }

            if (requestUpdate.getStatus() == RequestStatus.CONFIRMED) {
                if (limit != 0 && confirmedCount >= limit) {
                    throw new ConditionsNotMetException("Participant limit has been reached") {
                    };
                }

                r.setStatus(RequestStatus.CONFIRMED);
                confirmedCount++;
                event.setConfirmedRequests(confirmedCount);
                requestRepository.save(r);
                confirmed.add(requestMapper.toDto(r));

            } else {
                r.setStatus(RequestStatus.REJECTED);
                requestRepository.save(r);
                rejected.add(requestMapper.toDto(r));
            }
        }

        eventRepository.save(event);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }

    private Event getEventForInitiator(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        "Event with id=" + eventId + " was not found") {
                });
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException(
                    "Event with id=" + eventId + " was not found") {
            };
        }
        return event;
    }

    private LocalDateTime truncatedToMicros() {
        LocalDateTime now = LocalDateTime.now();
        int nanos = now.getNano();
        int micros = nanos / 1_000;
        return now.withNano(micros * 1_000);
    }
}

