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
    email                 VARCHAR(255) UNIQUE,
    occupation            VARCHAR(50),
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
    sponsor_collectivity_id INTEGER     REFERENCES collectivity (id) ON DELETE RESTRICT,
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

CREATE TABLE IF NOT EXISTS membership_fee
(
    id              SERIAL PRIMARY KEY,
    collectivity_id INTEGER NOT NULL REFERENCES collectivity (id) ON DELETE CASCADE,
    eligible_from   DATE    NOT NULL,
    frequency       VARCHAR(20) NOT NULL CHECK (frequency IN ('WEEKLY', 'MONTHLY', 'ANNUALLY', 'PUNCTUALLY')),
    amount          DECIMAL(12, 2) NOT NULL CHECK (amount >= 0),
    label           VARCHAR(255),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at      TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS financial_account
(
    id                   SERIAL PRIMARY KEY,
    collectivity_id      INTEGER NOT NULL REFERENCES collectivity (id) ON DELETE CASCADE,
    account_type         VARCHAR(20) NOT NULL CHECK (account_type IN ('CASH', 'MOBILE_BANKING', 'BANK')),
    holder_name          VARCHAR(255),
    mobile_banking_service VARCHAR(20) CHECK (mobile_banking_service IN ('AIRTEL_MONEY', 'MVOLA', 'ORANGE_MONEY')),
    mobile_number        VARCHAR(20),
    bank_name            VARCHAR(50) CHECK (bank_name IN ('BRED', 'MCB', 'BMOI', 'BOA', 'BGFI', 'AFG', 'ACCES_BAQUE', 'BAOBAB', 'SIPEM')),
    bank_code            INTEGER,
    bank_branch_code     INTEGER,
    bank_account_number  VARCHAR(20),
    bank_account_key     INTEGER,
    amount               DECIMAL(12, 2) NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS collectivity_transaction
(
    id                      SERIAL PRIMARY KEY,
    collectivity_id         INTEGER NOT NULL REFERENCES collectivity (id) ON DELETE CASCADE,
    member_id               INTEGER NOT NULL REFERENCES member (id) ON DELETE RESTRICT,
    membership_fee_id       INTEGER NOT NULL REFERENCES membership_fee (id) ON DELETE RESTRICT,
    financial_account_id    INTEGER NOT NULL REFERENCES financial_account (id) ON DELETE RESTRICT,
    amount                  DECIMAL(12, 2) NOT NULL CHECK (amount > 0),
    payment_mode            VARCHAR(20) NOT NULL CHECK (payment_mode IN ('CASH', 'MOBILE_BANKING', 'BANK_TRANSFER')),
    payment_date            DATE NOT NULL DEFAULT CURRENT_DATE,
    status                  VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    transaction_reference   VARCHAR(255),
    created_at              TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS member_payment
(
    id                      SERIAL PRIMARY KEY,
    member_id               INTEGER NOT NULL REFERENCES member (id) ON DELETE CASCADE,
    membership_fee_id       INTEGER NOT NULL REFERENCES membership_fee (id) ON DELETE RESTRICT,
    financial_account_id    INTEGER NOT NULL REFERENCES financial_account (id) ON DELETE RESTRICT,
    amount                  DECIMAL(12, 2) NOT NULL CHECK (amount > 0),
    payment_mode            VARCHAR(20) NOT NULL CHECK (payment_mode IN ('CASH', 'MOBILE_BANKING', 'BANK_TRANSFER')),
    payment_date            DATE NOT NULL DEFAULT CURRENT_DATE,
    status                  VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    created_at              TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_membership_fee_collectivity ON membership_fee(collectivity_id);
CREATE INDEX IF NOT EXISTS idx_financial_account_collectivity ON financial_account(collectivity_id);
CREATE INDEX IF NOT EXISTS idx_transaction_collectivity ON collectivity_transaction(collectivity_id);
CREATE INDEX IF NOT EXISTS idx_transaction_member ON collectivity_transaction(member_id);
CREATE INDEX IF NOT EXISTS idx_member_payment_member ON member_payment(member_id);