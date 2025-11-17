package ru.practicum.ewm.main.request.model;


import jakarta.persistence.*;
import lombok.*;
import ru.practicum.ewm.main.event.Event;
import ru.practicum.ewm.main.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests", uniqueConstraints = {@UniqueConstraint(name = "uq_requests_requester_event",
        columnNames = {"requester_id", "event_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "created", nullable = false, updatable = false)
    private LocalDateTime created;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RequestStatus status;

}
