package ru.practicum.ewm.main.request.dto;

import lombok.*;
import ru.practicum.ewm.main.request.model.RequestStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipationRequestDto {

    private Long id;
    private Long requester;
    private Long event;
    private RequestStatus status;
    private LocalDateTime created;

}
