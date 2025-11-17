package ru.practicum.ewm.main.compilation.service;

import ru.practicum.ewm.main.compilation.dto.CompilationDto;
import ru.practicum.ewm.main.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.main.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto addCompilation(NewCompilationDto dto);

    void deleteCompilation(Long id);

    CompilationDto updateCompilation(Long id, UpdateCompilationRequest dto);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilation(Long id);
}
