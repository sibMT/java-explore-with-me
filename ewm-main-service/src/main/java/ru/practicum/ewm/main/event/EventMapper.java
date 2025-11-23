package ru.practicum.ewm.main.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.main.category.dto.CategoryDto;
import ru.practicum.ewm.main.category.mapper.CategoryMapper;
import ru.practicum.ewm.main.category.model.Category;
import ru.practicum.ewm.main.event.dto.*;
import ru.practicum.ewm.main.user.dto.UserShortDto;
import ru.practicum.ewm.main.user.model.User;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final CategoryMapper categoryMapper;

    public Event toEntity(NewEventDto dto, User initiator, Category category) {
        if (dto == null) {
            return null;
        }
        Event event = new Event();
        event.setAnnotation(dto.getAnnotation());
        event.setDescription(dto.getDescription());
        event.setTitle(dto.getTitle());

        event.setCategory(category);
        event.setInitiator(initiator);

        event.setEventDate(dto.getEventDate());
        event.setCreatedOn(LocalDateTime.now());

        event.setPaid(Boolean.TRUE.equals(dto.getPaid()));
        event.setParticipantLimit(dto.getParticipantLimit() != null ? dto.getParticipantLimit() : 0);
        event.setRequestModeration(dto.getRequestModeration() == null || dto.getRequestModeration());
        event.setConfirmedRequests(0);

        if (dto.getLocation() != null) {
            event.setLocationLat(dto.getLocation().getLat());
            event.setLocationLon(dto.getLocation().getLon());
        }
        return event;
    }

    public EventShortDto toShortDto(Event event, long views) {
        if (event == null) {
            return null;
        }
        CategoryDto categoryDto = categoryMapper.toDto(event.getCategory());
        UserShortDto initiatorDto = toUserShortDto(event.getInitiator());

        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .title(event.getTitle())
                .category(categoryDto)
                .initiator(initiatorDto)
                .eventDate(event.getEventDate())
                .paid(event.isPaid())
                .confirmedRequests(event.getConfirmedRequests())
                .views(views)
                .build();
    }

    public EventFullDto toFullDto(Event event, long views) {
        if (event == null) {
            return null;
        }
        CategoryDto categoryDto = categoryMapper.toDto(event.getCategory());
        UserShortDto initiatorDto = toUserShortDto(event.getInitiator());

        EventLocationDto location = null;
        if (event.getLocationLat() != null && event.getLocationLon() != null) {
            location = EventLocationDto.builder()
                    .lat(event.getLocationLat())
                    .lon(event.getLocationLon())
                    .build();
        }

        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .title(event.getTitle())
                .category(categoryDto)
                .initiator(initiatorDto)
                .eventDate(event.getEventDate())
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .paid(event.isPaid())
                .participantLimit(event.getParticipantLimit())
                .confirmedRequests(event.getConfirmedRequests())
                .requestModeration(event.isRequestModeration())
                .views(views)
                .location(location)
                .state(event.getState())
                .build();
    }

    public void updateEventFromUserRequest(UpdateEventUserRequest dto, Event event) {
        if (dto == null || event == null) {
            return;
        }

        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        if (dto.getEventDate() != null) {
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getLocation() != null) {
            event.setLocationLat(dto.getLocation().getLat());
            event.setLocationLon(dto.getLocation().getLon());
        }
    }


    private UserShortDto toUserShortDto(User user) {
        if (user == null) {
            return null;
        }
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public void updateEventFromAdminRequest(UpdateEventAdminRequest dto, Event event) {
        if (dto == null || event == null) {
            return;
        }

        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        if (dto.getEventDate() != null) {
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
    }

}

