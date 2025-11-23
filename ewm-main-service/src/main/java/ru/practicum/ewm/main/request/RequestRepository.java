package ru.practicum.ewm.main.request;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.main.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByRequesterId(Long requesterId);

    List<Request> findAllByEventId(Long eventId);

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

}
