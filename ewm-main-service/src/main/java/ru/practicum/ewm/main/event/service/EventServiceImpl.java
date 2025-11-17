package ru.practicum.ewm.main.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.category.repository.CategoryRepository;
import ru.practicum.ewm.main.category.model.Category;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.event.EventState;
import ru.practicum.ewm.main.event.dto.*;
import ru.practicum.ewm.main.event.EventMapper;
import ru.practicum.ewm.main.event.Event;
import ru.practicum.ewm.main.event.dto.EventSort;
import ru.practicum.ewm.main.event.UserStateAction;
import ru.practicum.ewm.main.exception.BadRequestException;
import ru.practicum.ewm.main.exception.ConditionsNotMetException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.user.repository.UserRepository;
import ru.practicum.ewm.main.user.model.User;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.main.event.AdminStateAction;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;

    private static final String APP_NAME = "ewm-main-service";

    @Override
    public List<EventShortDto> getPublicEvents(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd,
                                               Boolean onlyAvailable,
                                               EventSort sort,
                                               int from,
                                               int size,
                                               String clientIp,
                                               String requestUri) {

        saveHit(clientIp, requestUri);

        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("rangeEnd must be after rangeStart");
        }

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (sort == null) {
            sort = EventSort.EVENT_DATE;
        }
        boolean onlyAvailableFlag = Boolean.TRUE.equals(onlyAvailable);

        int page = from / size;

        Sort springSort = (sort == EventSort.EVENT_DATE)
                ? Sort.by("eventDate").ascending()
                : Sort.by("id");

        Pageable pageable = PageRequest.of(page, size, springSort);

        Specification<Event> spec = Specification.where(isPublished())
                .and(dateAfter(rangeStart));

        if (text != null && !text.isBlank()) {
            spec = spec.and(textSearch(text));
        }
        if (categories != null && !categories.isEmpty()) {
            spec = spec.and(categoryIn(categories));
        }
        if (paid != null) {
            spec = spec.and(paidEq(paid));
        }
        if (rangeEnd != null) {
            spec = spec.and(dateBefore(rangeEnd));
        }
        if (onlyAvailableFlag) {
            spec = spec.and(onlyAvailableSpec());
        }

        Page<Event> eventsPage = eventRepository.findAll(spec, pageable);
        List<Event> events = eventsPage.getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> views = getViewsForEvents(events);

        List<EventShortDto> result = events.stream()
                .map(e -> eventMapper.toShortDto(e, views.getOrDefault(e.getId(), 0L)))
                .toList();

        if (sort == EventSort.VIEWS) {
            result = result.stream()
                    .sorted(Comparator.comparingLong(
                                    (EventShortDto dto) -> Optional.ofNullable(dto.getViews()).orElse(0L)
                            ).reversed()
                    )
                    .toList();
        }

        return result;
    }


    @Override
    public EventFullDto getPublicEvent(Long eventId, String clientIp, String requestUri) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new NotFoundException("Event with id=" + eventId + " was not found") {
                        });

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + eventId + " was not found") {
            };
        }

        saveHit(clientIp, requestUri);

        long views = getViewsForEvent(eventId);
        return eventMapper.toFullDto(event, views);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getAdminEvents(List<Long> users,
                                             List<EventState> states,
                                             List<Long> categories,
                                             LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd,
                                             int from,
                                             int size) {

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Specification<Event> spec = Specification.where(null);

        if (users != null && !users.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.get("initiator").get("id").in(users));
        }

        if (states != null && !states.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.get("state").in(states));
        }

        if (categories != null && !categories.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.get("category").get("id").in(categories));
        }

        if (rangeStart != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }

        if (rangeEnd != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }

        Page<Event> eventPage = eventRepository.findAll(spec, pageable);
        List<Event> events = eventPage.getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> views = getViewsForEvents(events);

        return events.stream()
                .map(e -> eventMapper.toFullDto(e, views.getOrDefault(e.getId(), 0L)))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest dto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        "Event with id=" + eventId + " was not found") {
                });

        if (dto.getEventDate() != null &&
                dto.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ConditionsNotMetException(
                    "Event date must be at least 1 hour from now") {
            };
        }

        eventMapper.updateEventFromAdminRequest(dto, event);

        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException(
                            "Category with id=" + dto.getCategory() + " was not found") {
                    });
            event.setCategory(category);
        }

        if (dto.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
            if (event.getState() != EventState.PENDING) {
                throw new ConditionsNotMetException(
                        "Only pending events can be published") {
                };
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        } else if (dto.getStateAction() == AdminStateAction.REJECT_EVENT) {
            if (event.getState() == EventState.PUBLISHED) {
                throw new ConditionsNotMetException(
                        "Published events cannot be rejected") {
                };
            }
            event.setState(EventState.CANCELED);
        }

        Event saved = eventRepository.save(event);
        long views = getViewsForEvent(eventId);
        return eventMapper.toFullDto(saved, views);
    }


    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {

        if (dto.getEventDate() != null &&
                dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date must be at least 2 hours from now");
        }

        Event event = new Event();
        event.setAnnotation(dto.getAnnotation());
        event.setDescription(dto.getDescription());
        event.setTitle(dto.getTitle());

        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() ->
                        new NotFoundException("Category with id=" + dto.getCategory() + " was not found")
                );
        event.setCategory(category);

        event.setEventDate(dto.getEventDate());
        event.setPaid(Boolean.TRUE.equals(dto.getPaid()));
        event.setParticipantLimit(
                dto.getParticipantLimit() == null ? 0 : dto.getParticipantLimit()
        );

        event.setRequestModeration(
                dto.getRequestModeration() == null || dto.getRequestModeration()
        );

        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event.setLocationLat(dto.getLocation().getLat());
        event.setLocationLon(dto.getLocation().getLon());

        User initiator = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("User with id=" + userId + " was not found")
                );
        event.setInitiator(initiator);

        Event saved = eventRepository.save(event);
        return eventMapper.toFullDto(saved, 0L);
    }


    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<Event> pageEvents = eventRepository.findByInitiatorId(userId, pageable);
        List<Event> events = pageEvents.getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> views = getViewsForEvents(events);

        return events.stream()
                .map(e -> eventMapper.toShortDto(e, views.getOrDefault(e.getId(), 0L)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = findUserEventOrThrow(userId, eventId);
        long views = getViewsForEvent(eventId);
        return eventMapper.toFullDto(event, views);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest dto) {
        Event event = findUserEventOrThrow(userId, eventId);

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConditionsNotMetException("Cannot edit a published event");
        }

        if (dto.getEventDate() != null &&
                dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date must be at least 2 hours from now");
        }

        eventMapper.updateEventFromUserRequest(dto, event);

        if (dto.getCategory() != null) {
            Category category = categoryRepository.findById(dto.getCategory())
                    .orElseThrow(() -> new NotFoundException(
                            "Category with id=" + dto.getCategory() + " was not found") {
                    });
            event.setCategory(category);
        }

        if (dto.getStateAction() == UserStateAction.SEND_TO_REVIEW) {
            event.setState(EventState.PENDING);
        } else if (dto.getStateAction() == UserStateAction.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        }

        Event saved = eventRepository.save(event);
        long views = getViewsForEvent(eventId);
        return eventMapper.toFullDto(saved, views);
    }

    private Event findUserEventOrThrow(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new NotFoundException("Event with id=" + eventId + " was not found") {
                        });

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found") {
            };
        }
        return event;
    }

    private void saveHit(String clientIp, String uri) {
        statsClient.postHit(APP_NAME, uri, clientIp, LocalDateTime.now());
    }

    private Map<Long, Long> getViewsForEvents(List<Event> events) {
        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusYears(1));

        LocalDateTime end = LocalDateTime.now();

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true);

        Map<String, Long> uriToHits = stats.stream()
                .collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits));

        Map<Long, Long> result = new HashMap<>();
        for (Event e : events) {
            Long hits = uriToHits.getOrDefault("/events/" + e.getId(), 0L);
            result.put(e.getId(), hits);
        }
        return result;
    }

    private long getViewsForEvent(Long eventId) {
        LocalDateTime start = LocalDateTime.now().minusYears(1);
        LocalDateTime end = LocalDateTime.now();
        String uri = "/events/" + eventId;

        List<ViewStatsDto> stats = statsClient.getStats(start, end, List.of(uri), true);
        if (stats.isEmpty()) {
            return 0L;
        }
        return stats.get(0).getHits();
    }

    private Specification<Event> isPublished() {
        return (root, query, cb) -> cb.equal(root.get("state"), EventState.PUBLISHED);
    }

    private Specification<Event> dateAfter(LocalDateTime from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("eventDate"), from);
    }

    private Specification<Event> dateBefore(LocalDateTime to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("eventDate"), to);
    }

    private Specification<Event> textSearch(String text) {
        String pattern = "%" + text.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("annotation")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
        );
    }

    private Specification<Event> categoryIn(List<Long> categoryIds) {
        return (root, query, cb) -> root.get("category").get("id").in(categoryIds);
    }

    private Specification<Event> paidEq(boolean paid) {
        return (root, query, cb) -> cb.equal(root.get("paid"), paid);
    }

    private Specification<Event> onlyAvailableSpec() {
        return (root, query, cb) -> cb.or(
                cb.equal(root.get("participantLimit"), 0),
                cb.lessThan(root.get("confirmedRequests"), root.get("participantLimit"))
        );
    }
}


