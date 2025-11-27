package ru.practicum.ewm.main.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.main.compilation.dto.CompilationDto;
import ru.practicum.ewm.main.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.main.event.Event;
import ru.practicum.ewm.main.event.EventMapper;
import ru.practicum.ewm.main.event.dto.EventShortDto;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CompilationMapper {
    private final EventMapper eventMapper;

    public Compilation toEntity(NewCompilationDto dto, Set<Event> events) {
        if (dto == null) return null;
        Compilation compilation = new Compilation();
        compilation.setTitle(dto.getTitle());
        compilation.setPinned(dto.getPinned() != null && dto.getPinned());
        if (events != null) {
            compilation.setEvents(events);
        }
        return compilation;
    }

    public CompilationDto toDto(Compilation compilation) {
        if (compilation == null) return null;

        List<EventShortDto> eventShortDtos = compilation.getEvents().stream()
                .map(e -> {
                    double rating = e.getRating() != null ? e.getRating() : 0.0;
                    return eventMapper.toShortDto(e, 0L, rating);
                })
                .toList();

        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .events(eventShortDtos)
                .build();
    }
}
