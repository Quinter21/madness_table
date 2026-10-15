CREATE TABLE app_info (
    id  SERIAL PRIMARY KEY,
    key VARCHAR(100) NOT NULL UNIQUE,
    value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP  NOT NULL DEFAULT now()
);

INSERT INTO app_info (key, value) VALUES ('schema_version', 'init');