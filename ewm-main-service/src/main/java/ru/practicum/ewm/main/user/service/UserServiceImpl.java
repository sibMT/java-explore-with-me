package ru.practicum.ewm.main.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.user.dto.NewUserRequest;
import ru.practicum.ewm.main.user.dto.UserDto;
import ru.practicum.ewm.main.user.mapper.UserMapper;
import ru.practicum.ewm.main.user.model.User;
import ru.practicum.ewm.main.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest dto) {
        if (repository.existsByEmail(dto.getEmail())) {
            throw new DataIntegrityViolationException("User email must be unique");
        }
        User user = mapper.toEntity(dto);
        User saved = repository.save(user);
        return mapper.toDto(saved);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        int page = from / size;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").ascending());

        if (ids == null || ids.isEmpty()) {
            return repository.findAll(pageRequest)
                    .map(mapper::toDto)
                    .toList();
        }

        return repository.findAllByIdIn(ids, Sort.by("id").ascending())
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!repository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found") {
            };
        }
        repository.deleteById(userId);
    }

}
