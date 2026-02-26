CREATE TABLE IF NOT EXISTS lost_pet_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    pet_name VARCHAR(100) NOT NULL,
    breed VARCHAR(100),
    photo_url VARCHAR(500) NOT NULL,
    description VARCHAR(2000),
    last_seen_at DATETIME NOT NULL,
    last_seen_location VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    resolved_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE INDEX idx_lost_pet_report_owner_status
    ON lost_pet_report (owner_id, status);

CREATE INDEX idx_lost_pet_report_status_last_seen_at
    ON lost_pet_report (status, last_seen_at);

CREATE TABLE IF NOT EXISTS sighting (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    finder_id BIGINT NOT NULL,
    photo_url VARCHAR(500) NOT NULL,
    found_at DATETIME NOT NULL,
    found_location VARCHAR(255) NOT NULL,
    memo VARCHAR(2000),
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE INDEX idx_sighting_finder_found_at
    ON sighting (finder_id, found_at);

CREATE INDEX idx_sighting_status_found_at
    ON sighting (status, found_at);

CREATE TABLE IF NOT EXISTS lost_pet_match (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lost_pet_id BIGINT NOT NULL,
    sighting_id BIGINT NOT NULL,
    similarity_total DECIMAL(5, 4) NOT NULL,
    status VARCHAR(40) NOT NULL,
    approved_by_member_id BIGINT,
    approved_at DATETIME,
    chat_room_id BIGINT,
    invalidated_reason VARCHAR(40),
    invalidated_at DATETIME,
    matched_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_lost_pet_match_pair UNIQUE (lost_pet_id, sighting_id),
    CONSTRAINT fk_lost_pet_match_lost_pet
        FOREIGN KEY (lost_pet_id) REFERENCES lost_pet_report(id),
    CONSTRAINT fk_lost_pet_match_sighting
        FOREIGN KEY (sighting_id) REFERENCES sighting(id)
);

CREATE INDEX idx_lost_pet_match_lost_pet_status
    ON lost_pet_match (lost_pet_id, status);

CREATE INDEX idx_lost_pet_match_sighting_status
    ON lost_pet_match (sighting_id, status);
