CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(512) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    CONSTRAINT uq_category_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS events (
    id                   BIGSERIAL PRIMARY KEY,
    annotation           VARCHAR(2000) NOT NULL,
    description          VARCHAR(7000) NOT NULL,
    title                VARCHAR(120)  NOT NULL,

    category_id          BIGINT NOT NULL REFERENCES categories(id),
    initiator_id         BIGINT NOT NULL REFERENCES users(id),

    event_date           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_on           TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    published_on         TIMESTAMP WITHOUT TIME ZONE,
    state                VARCHAR(16) NOT NULL,
    paid                 BOOLEAN NOT NULL DEFAULT FALSE,
    participant_limit    INTEGER NOT NULL DEFAULT 0,
    request_moderation   BOOLEAN NOT NULL DEFAULT TRUE,
    confirmed_requests   INTEGER NOT NULL DEFAULT 0,
    location_lat         NUMERIC(10, 6),
    location_lon         NUMERIC(10, 6)
);

CREATE INDEX IF NOT EXISTS idx_events_state_event_date
    ON events (state, event_date);

CREATE INDEX IF NOT EXISTS idx_events_category
    ON events (category_id);

CREATE INDEX IF NOT EXISTS idx_events_initiator
    ON events (initiator_id);

CREATE TABLE IF NOT EXISTS requests (
    id              BIGSERIAL PRIMARY KEY,
    requester_id    BIGINT NOT NULL REFERENCES users(id),
    event_id        BIGINT NOT NULL REFERENCES events(id),
    created         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    status          VARCHAR(16) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_requests_requester_event
    ON requests (requester_id, event_id);

CREATE INDEX IF NOT EXISTS idx_requests_event_status
    ON requests (event_id, status);

CREATE TABLE IF NOT EXISTS compilations (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(50) NOT NULL,
    pinned      BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_compilation_name UNIQUE (title)
);

CREATE TABLE IF NOT EXISTS compilation_events (
    compilation_id   BIGINT NOT NULL REFERENCES compilations(id) ON DELETE CASCADE,
    event_id         BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    PRIMARY KEY (compilation_id, event_id)
);

CREATE INDEX IF NOT EXISTS idx_compilations_pinned
    ON compilations (pinned);

CREATE TABLE IF NOT EXISTS ratings (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    NOT NULL,
    event_id    BIGINT    NOT NULL,
    is_like     BOOLEAN   NOT NULL,
    CONSTRAINT uq_rating_user_event UNIQUE (user_id, event_id),
    CONSTRAINT fk_ratings_user_id    FOREIGN KEY (user_id)  REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT fk_ratings_event_id   FOREIGN KEY (event_id) REFERENCES events(id)  ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_ratings_event_id ON ratings (event_id);
CREATE INDEX IF NOT EXISTS idx_ratings_event_id_is_like ON ratings (event_id, is_like);

