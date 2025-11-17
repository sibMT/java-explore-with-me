package ru.practicum.ewm.main.compilation.dto;

import lombok.*;
import ru.practicum.ewm.main.event.dto.EventShortDto;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompilationDto {
    private Long id;
    private String title;
    private boolean pinned;
    private List<EventShortDto> events;
}
