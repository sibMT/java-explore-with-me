package ru.practicum.ewm.main.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.compilation.Compilation;
import ru.practicum.ewm.main.compilation.CompilationMapper;
import ru.practicum.ewm.main.compilation.CompilationRepository;
import ru.practicum.ewm.main.compilation.dto.CompilationDto;
import ru.practicum.ewm.main.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.main.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.main.event.Event;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.exception.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto dto) {
        Set<Event> events = new HashSet<>();
        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            events = new HashSet<>(eventRepository.findAllById(dto.getEvents()));
        }

        Compilation compilation = compilationMapper.toEntity(dto, events);
        Compilation saved = compilationRepository.save(compilation);

        return compilationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found") {
                });
        compilationRepository.delete(compilation);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        "Compilation with id=" + compId + " was not found") {
                });

        if (dto == null) {
            return compilationMapper.toDto(compilation);
        }

        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }
        if (dto.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(dto.getEvents()));
            compilation.setEvents(events);
        }

        Compilation updated = compilationRepository.save(compilation);
        return compilationMapper.toDto(updated);
    }


    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        int page = from / size;
        var pageable = PageRequest.of(page, size);

        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(pageable).getContent();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, pageable).getContent();
        }

        return compilations.stream()
                .map(compilationMapper::toDto)
                .toList();
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found") {
                });
        return compilationMapper.toDto(compilation);
    }

}
