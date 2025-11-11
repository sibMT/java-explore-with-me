CREATE TABLE IF NOT EXISTS hits (
  id BIGSERIAL PRIMARY KEY,
  app VARCHAR(100) NOT NULL,
  uri TEXT NOT NULL,
  ip VARCHAR(45) NOT NULL,
  timestamp TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_hits_ts ON hits (timestamp);
CREATE INDEX IF NOT EXISTS idx_hits_uri_ts ON hits (uri, timestamp);
CREATE INDEX IF NOT EXISTS idx_hits_ip_uri_ts ON hits (ip, uri, timestamp);