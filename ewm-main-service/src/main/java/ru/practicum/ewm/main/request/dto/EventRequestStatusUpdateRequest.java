package ru.practicum.ewm.main.request.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.ewm.main.request.model.RequestStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestStatusUpdateRequest {
    @NotEmpty
    private List<Long> requestIds;
    @NotNull
    private RequestStatus status;
}
