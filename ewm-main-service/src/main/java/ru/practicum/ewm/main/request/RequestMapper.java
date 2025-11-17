package ru.practicum.ewm.main.request;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.main.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.main.request.model.Request;

@Component
public class RequestMapper {
    public ParticipationRequestDto toDto(Request request) {
        if (request == null) return null;
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .requester(request.getRequester().getId())
                .event(request.getEvent().getId())
                .status(request.getStatus())
                .created(request.getCreated())
                .build();
    }
}
