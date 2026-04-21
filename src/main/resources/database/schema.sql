CREATE TABLE IF NOT EXISTS federation
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS collectivity
(
    id                     SERIAL PRIMARY KEY,
    name                   VARCHAR(255) NOT NULL UNIQUE,
    location               VARCHAR(255) NOT NULL,
    agricultural_specialty VARCHAR(255) NOT NULL,
    registration_number    INTEGER      NOT NULL UNIQUE,
    creation_date          DATE         NOT NULL DEFAULT CURRENT_DATE,
    federation_approval    BOOLEAN      NOT NULL DEFAULT FALSE,
    federation_id          INTEGER      REFERENCES federation (id) ON DELETE SET NULL,
    created_at             TIMESTAMPTZ           DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMPTZ           DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS member
(
    id                    SERIAL PRIMARY KEY,
    first_name            VARCHAR(255) NOT NULL,
    last_name             VARCHAR(255) NOT NULL,
    birth_date            DATE         NOT NULL,
    gender                VARCHAR(10)  NOT NULL CHECK (gender IN ('MALE', 'FEMALE')),
    address               TEXT,
    profession            VARCHAR(255),
    phone_number          VARCHAR(20)  NOT NULL,
    email                 VARCHAR(255),
    join_date             DATE         NOT NULL DEFAULT CURRENT_DATE,
    registration_fee_paid BOOLEAN      NOT NULL DEFAULT FALSE,
    membership_dues_paid  BOOLEAN      NOT NULL DEFAULT FALSE,
    payment_proof_ref     VARCHAR(255),
    admission_status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING' CHECK (admission_status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at            TIMESTAMPTZ           DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ           DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS membership
(
    id              SERIAL PRIMARY KEY,
    member_id       INTEGER NOT NULL REFERENCES member (id) ON DELETE CASCADE,
    collectivity_id INTEGER NOT NULL REFERENCES collectivity (id) ON DELETE CASCADE,
    joined_at       DATE    NOT NULL DEFAULT CURRENT_DATE,
    created_at      TIMESTAMPTZ      DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (member_id, collectivity_id)
);

CREATE TABLE IF NOT EXISTS sponsorship
(
    id                      SERIAL PRIMARY KEY,
    member_id               INTEGER     NOT NULL REFERENCES member (id) ON DELETE CASCADE,
    sponsor_member_id       INTEGER     NOT NULL REFERENCES member (id) ON DELETE RESTRICT,
    sponsor_collectivity_id INTEGER     NOT NULL REFERENCES collectivity (id) ON DELETE RESTRICT,
    relationship            VARCHAR(50) NOT NULL CHECK (relationship IN ('FAMILY', 'FRIENDS', 'COLLEAGUES', 'OTHER')),
    created_at              TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (member_id, sponsor_member_id)
);

CREATE TABLE IF NOT EXISTS role
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO role (name)
VALUES ('PRESIDENT'),
       ('VICE_PRESIDENT'),
       ('TREASURER'),
       ('SECRETARY'),
       ('SENIOR'),
       ('JUNIOR')
ON CONFLICT (name) DO NOTHING;

CREATE TABLE IF NOT EXISTS mandate
(
    id         SERIAL PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date   DATE NOT NULL CHECK (end_date > start_date),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS assignment
(
    id              SERIAL PRIMARY KEY,
    member_id       INTEGER NOT NULL REFERENCES member (id) ON DELETE CASCADE,
    role_id         INTEGER NOT NULL REFERENCES role (id) ON DELETE RESTRICT,
    mandate_id      INTEGER NOT NULL REFERENCES mandate (id) ON DELETE RESTRICT,
    collectivity_id INTEGER REFERENCES collectivity (id) ON DELETE CASCADE,
    federation_id   INTEGER REFERENCES federation (id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CHECK (
        (collectivity_id IS NOT NULL AND federation_id IS NULL) OR
        (collectivity_id IS NULL AND federation_id IS NOT NULL)
        )
);