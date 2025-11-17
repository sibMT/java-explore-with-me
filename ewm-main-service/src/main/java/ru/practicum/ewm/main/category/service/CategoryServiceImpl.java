package ru.practicum.ewm.main.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.category.dto.CategoryDto;
import ru.practicum.ewm.main.category.dto.NewCategoryDto;
import ru.practicum.ewm.main.category.mapper.CategoryMapper;
import ru.practicum.ewm.main.category.model.Category;
import ru.practicum.ewm.main.category.repository.CategoryRepository;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.exception.ConditionsNotMetException;
import ru.practicum.ewm.main.exception.NotFoundException;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final EventRepository eventRepository;
    private final CategoryMapper mapper;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto dto) {
        if (repository.existsByName(dto.getName())) {
            throw new DataIntegrityViolationException("Категория должна быть уникальной");
        }
        Category category = mapper.fromNewDto(dto);
        Category saved;
        try {
            saved = repository.save(category);
        } catch (DataIntegrityViolationException ex) {
            throw ex;
        }
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id=" + id + " was not found") {
                });

        if (dto.getName() != null
                && !dto.getName().equals(category.getName())
                && repository.existsByName(dto.getName())) {
            throw new DataIntegrityViolationException("Категория должна быть уникальной");
        }

        mapper.updateEntity(dto, category);
        Category updated = repository.save(category);
        return mapper.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id=" + id + " was not found") {
                });

        if (eventRepository.existsByCategoryId(id)) {
            throw new ConditionsNotMetException("Category cannot be deleted because it is used by events") {
            };
        }

        repository.delete(category);
    }


    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        int page = from / size;
        PageRequest pageRequest =
                PageRequest.of(page, size, Sort.by("id").ascending());

        return repository.findAll(pageRequest)
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id=" + id + " was not found") {
                });
        return mapper.toDto(category);
    }
}
