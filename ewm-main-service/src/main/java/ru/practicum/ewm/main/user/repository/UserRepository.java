package ru.practicum.ewm.main.user.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.main.user.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    List<User> findAllByIdIn(List<Long> ids, Sort sort);
}

