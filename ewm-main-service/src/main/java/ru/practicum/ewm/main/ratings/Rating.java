package ru.practicum.ewm.main.ratings;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.ewm.main.event.Event;
import ru.practicum.ewm.main.user.model.User;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ratings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_event", columnNames = {"user_id", "event_id"})})
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "is_like", nullable = false)
    private Boolean isLike;
}
