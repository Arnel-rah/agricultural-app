-- =====================================================
-- SCRIPT COMPLET - CRÉATION DES TABLES ET INSERTION
-- FÉDÉRATION AGRICOLE DE MADAGASCAR
-- =====================================================

-- Suppression des tables existantes
DROP TABLE IF EXISTS attendance CASCADE;
DROP TABLE IF EXISTS activity CASCADE;
DROP TABLE IF EXISTS collectivity_transaction CASCADE;
DROP TABLE IF EXISTS member_payment CASCADE;
DROP TABLE IF EXISTS financial_account_balance_history CASCADE;
DROP TABLE IF EXISTS financial_account CASCADE;
DROP TABLE IF EXISTS membership_fee CASCADE;
DROP TABLE IF EXISTS assignment CASCADE;
DROP TABLE IF EXISTS sponsorship CASCADE;
DROP TABLE IF EXISTS membership CASCADE;
DROP TABLE IF EXISTS member CASCADE;
DROP TABLE IF EXISTS collectivity CASCADE;
DROP TABLE IF EXISTS mandate CASCADE;
DROP TABLE IF EXISTS role CASCADE;
DROP TABLE IF EXISTS federation CASCADE;

-- =====================================================
-- CRÉATION DES TABLES
-- =====================================================

CREATE TABLE federation
(
    id   VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE collectivity
(
    id                     VARCHAR(50) PRIMARY KEY,
    name                   VARCHAR(255) NOT NULL UNIQUE,
    location               VARCHAR(255) NOT NULL,
    agricultural_specialty VARCHAR(255) NOT NULL,
    registration_number    VARCHAR(50)  NOT NULL UNIQUE,
    creation_date          DATE         NOT NULL DEFAULT CURRENT_DATE,
    federation_approval    BOOLEAN      NOT NULL DEFAULT TRUE,
    federation_id          VARCHAR(50)  REFERENCES federation (id) ON DELETE SET NULL
);

CREATE TABLE member
(
    id                    VARCHAR(50) PRIMARY KEY,
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
    admission_status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING' CHECK (admission_status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE TABLE membership
(
    id              VARCHAR(50) PRIMARY KEY,
    member_id       VARCHAR(50) NOT NULL REFERENCES member (id) ON DELETE CASCADE,
    collectivity_id VARCHAR(50) NOT NULL REFERENCES collectivity (id) ON DELETE CASCADE,
    joined_at       DATE        NOT NULL DEFAULT CURRENT_DATE,
    UNIQUE (member_id, collectivity_id)
);

CREATE TABLE sponsorship
(
    id                      VARCHAR(50) PRIMARY KEY,
    member_id               VARCHAR(50) NOT NULL REFERENCES member (id) ON DELETE CASCADE,
    sponsor_member_id       VARCHAR(50) NOT NULL REFERENCES member (id) ON DELETE RESTRICT,
    sponsor_collectivity_id VARCHAR(50) REFERENCES collectivity (id) ON DELETE RESTRICT,
    relationship            VARCHAR(50) NOT NULL CHECK (relationship IN ('FAMILY', 'FRIENDS', 'COLLEAGUES', 'OTHER'))
);

CREATE TABLE role
(
    id   VARCHAR(50) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE mandate
(
    id         VARCHAR(50) PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date   DATE NOT NULL CHECK (end_date > start_date)
);

CREATE TABLE assignment
(
    id              VARCHAR(50) PRIMARY KEY,
    member_id       VARCHAR(50) NOT NULL REFERENCES member (id) ON DELETE CASCADE,
    role_id         VARCHAR(50) NOT NULL REFERENCES role (id) ON DELETE RESTRICT,
    mandate_id      VARCHAR(50) NOT NULL REFERENCES mandate (id) ON DELETE RESTRICT,
    collectivity_id VARCHAR(50) REFERENCES collectivity (id) ON DELETE CASCADE,
    federation_id   VARCHAR(50) REFERENCES federation (id) ON DELETE CASCADE,
    CHECK (
        (collectivity_id IS NOT NULL AND federation_id IS NULL) OR
        (collectivity_id IS NULL AND federation_id IS NOT NULL)
        )
);

CREATE TABLE membership_fee
(
    id              VARCHAR(50) PRIMARY KEY,
    collectivity_id VARCHAR(50)    NOT NULL REFERENCES collectivity (id) ON DELETE CASCADE,
    eligible_from   DATE           NOT NULL,
    frequency       VARCHAR(20)    NOT NULL CHECK (frequency IN ('WEEKLY', 'MONTHLY', 'ANNUALLY', 'PUNCTUALLY')),
    amount          DECIMAL(12, 2) NOT NULL CHECK (amount >= 0),
    label           VARCHAR(255),
    status          VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE financial_account
(
    id              VARCHAR(50) PRIMARY KEY,
    collectivity_id VARCHAR(50)    NOT NULL REFERENCES collectivity (id) ON DELETE CASCADE,
    account_type    VARCHAR(20)    NOT NULL CHECK (account_type IN ('CASH', 'MOBILE_BANKING', 'BANK')),
    holder_name     VARCHAR(255),
    mobile_number   VARCHAR(20),
    balance         DECIMAL(12, 2) NOT NULL DEFAULT 0
);

CREATE TABLE collectivity_transaction
(
    id                   VARCHAR(50) PRIMARY KEY,
    collectivity_id      VARCHAR(50)    NOT NULL REFERENCES collectivity (id) ON DELETE CASCADE,
    member_id            VARCHAR(50)    NOT NULL REFERENCES member (id) ON DELETE RESTRICT,
    membership_fee_id    VARCHAR(50)    NOT NULL REFERENCES membership_fee (id) ON DELETE RESTRICT,
    financial_account_id VARCHAR(50)    NOT NULL REFERENCES financial_account (id) ON DELETE RESTRICT,
    amount               DECIMAL(12, 2) NOT NULL CHECK (amount > 0),
    payment_mode         VARCHAR(20)    NOT NULL CHECK (payment_mode IN ('CASH', 'MOBILE_BANKING', 'BANK_TRANSFER')),
    payment_date         DATE           NOT NULL DEFAULT CURRENT_DATE,
    status               VARCHAR(20)    NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
);

CREATE TABLE member_payment
(
    id                   VARCHAR(50) PRIMARY KEY,
    member_id            VARCHAR(50)    NOT NULL REFERENCES member (id) ON DELETE CASCADE,
    membership_fee_id    VARCHAR(50)    NOT NULL REFERENCES membership_fee (id) ON DELETE RESTRICT,
    financial_account_id VARCHAR(50)    NOT NULL REFERENCES financial_account (id) ON DELETE RESTRICT,
    amount               DECIMAL(12, 2) NOT NULL CHECK (amount > 0),
    payment_mode         VARCHAR(20)    NOT NULL CHECK (payment_mode IN ('CASH', 'MOBILE_BANKING', 'BANK_TRANSFER')),
    payment_date         DATE           NOT NULL DEFAULT CURRENT_DATE,
    status               VARCHAR(20)    NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
);

-- Tables pour les activités (Bonus)
CREATE TABLE activity
(
    id                          VARCHAR(50) PRIMARY KEY,
    collectivity_id             VARCHAR(50) REFERENCES collectivity (id) ON DELETE CASCADE,
    label                       VARCHAR(255) NOT NULL,
    activity_type               VARCHAR(20)  NOT NULL CHECK (activity_type IN ('MEETING', 'TRAINING', 'OTHER')),
    member_occupation_concerned VARCHAR(50),
    recurrence_week_ordinal     INTEGER CHECK (recurrence_week_ordinal BETWEEN 1 AND 5),
    recurrence_day_of_week      VARCHAR(2) CHECK (recurrence_day_of_week IN ('MO', 'TU', 'WE', 'TH', 'FR', 'SA', 'SU')),
    executive_date              DATE,
    CHECK (
        (recurrence_week_ordinal IS NOT NULL AND recurrence_day_of_week IS NOT NULL AND executive_date IS NULL) OR
        (recurrence_week_ordinal IS NULL AND recurrence_day_of_week IS NULL AND executive_date IS NOT NULL)
        )
);

CREATE TABLE attendance
(
    id                VARCHAR(50) PRIMARY KEY,
    activity_id       VARCHAR(50) NOT NULL REFERENCES activity (id) ON DELETE CASCADE,
    member_id         VARCHAR(50) NOT NULL REFERENCES member (id) ON DELETE CASCADE,
    attendance_status VARCHAR(20) NOT NULL CHECK (attendance_status IN ('MISSING', 'ATTENDED', 'UNDEFINED')),
    UNIQUE (activity_id, member_id)
);

-- =====================================================
-- INDEX
-- =====================================================

CREATE INDEX idx_membership_fee_collectivity ON membership_fee (collectivity_id);
CREATE INDEX idx_financial_account_collectivity ON financial_account (collectivity_id);
CREATE INDEX idx_transaction_collectivity ON collectivity_transaction (collectivity_id);
CREATE INDEX idx_transaction_member ON collectivity_transaction (member_id);
CREATE INDEX idx_transaction_date ON collectivity_transaction (payment_date);
CREATE INDEX idx_member_payment_member ON member_payment (member_id);
CREATE INDEX idx_member_payment_member_fee ON member_payment (member_id, membership_fee_id, status);
CREATE INDEX idx_membership_joined_at ON membership (joined_at, collectivity_id);
CREATE INDEX idx_membership_fee_status ON membership_fee (collectivity_id, status);
CREATE INDEX idx_sponsorship_member ON sponsorship (member_id);
CREATE INDEX idx_sponsorship_sponsor ON sponsorship (sponsor_member_id);
CREATE INDEX idx_member_email ON member (email);
CREATE INDEX idx_member_join_date ON member (join_date);
CREATE INDEX idx_membership_collectivity ON membership (collectivity_id);
CREATE INDEX idx_assignment_collectivity ON assignment (collectivity_id);
CREATE INDEX idx_activity_collectivity ON activity (collectivity_id);
CREATE INDEX idx_attendance_activity ON attendance (activity_id);

-- =====================================================
-- INSERTION DES DONNÉES
-- =====================================================

-- Rôles
INSERT INTO role (id, name)
VALUES ('role-1', 'PRESIDENT'),
       ('role-2', 'VICE_PRESIDENT'),
       ('role-3', 'SECRETARY'),
       ('role-4', 'TREASURER'),
       ('role-5', 'SENIOR'),
       ('role-6', 'JUNIOR')
ON CONFLICT (id) DO NOTHING;

-- Fédération
INSERT INTO federation (id, name)
VALUES ('fed-1', 'Fédération Nationale des Agriculteurs de Madagascar')
ON CONFLICT (id) DO NOTHING;

-- Collectivités
INSERT INTO collectivity (id, name, location, agricultural_specialty, registration_number, creation_date,
                          federation_approval, federation_id)
VALUES ('col-1', 'Mpanorina', 'Ambatondrazaka', 'Riziculture', '1', '2026-01-01', TRUE, 'fed-1'),
       ('col-2', 'Dobo voalohany', 'Ambatondrazaka', 'Pisciculture', '2', '2026-01-01', TRUE, 'fed-1'),
       ('col-3', 'Tantely mamy', 'Brickaville', 'Apiculture', '3', '2026-01-01', TRUE, 'fed-1')
ON CONFLICT (id) DO NOTHING;

-- Mandat
INSERT INTO mandate (id, start_date, end_date)
VALUES ('man-1', '2026-01-01', '2026-12-31')
ON CONFLICT (id) DO NOTHING;

-- Membres - Collectivité 1
INSERT INTO member (id, last_name, first_name, birth_date, gender, address, profession, phone_number, email, occupation,
                    registration_fee_paid, membership_dues_paid, admission_status, join_date)
VALUES ('C1-M1', 'Nom membre 1', 'Prénom membre 1', '1980-02-01', 'MALE', 'Lot II V M Ambato', 'Riziculteur',
        '0341234567', 'member.col1.1@fed-agri.mg', 'PRESIDENT', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C1-M2', 'Nom membre 2', 'Prénom membre 2', '1982-03-05', 'MALE', 'Lot II F Ambato', 'Agriculteur',
        '0321234567', 'member.col1.2@fed-agri.mg', 'VICE_PRESIDENT', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C1-M3', 'Nom membre 3', 'Prénom membre 3', '1992-03-10', 'MALE', 'Lot II J Ambato', 'Collecteur', '0331234567',
        'member.col1.3@fed-agri.mg', 'SECRETARY', TRUE, TRUE, 'APPROVED', CURRENT_DATE - INTERVAL '9 months'),
       ('C1-M4', 'Nom membre 4', 'Prénom membre 4', '1988-05-22', 'FEMALE', 'Lot A K 50 Ambato', 'Distributeur',
        '0381234567', 'member.col1.4@fed-agri.mg', 'TREASURER', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C1-M5', 'Nom membre 5', 'Prénom membre 5', '1999-08-21', 'MALE', 'Lot UV 80 Ambato', 'Riziculteur',
        '0373434567', 'member.col1.5@fed-agri.mg', 'SENIOR', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C1-M6', 'Nom membre 6', 'Prénom membre 6', '1998-08-22', 'FEMALE', 'Lot UV 6 Ambato', 'Riziculteur',
        '0372234567', 'member.col1.6@fed-agri.mg', 'SENIOR', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C1-M7', 'Nom membre 7', 'Prénom membre 7', '1998-01-31', 'MALE', 'Lot UV 7 Ambato', 'Riziculteur',
        '0374234567', 'member.col1.7@fed-agri.mg', 'SENIOR', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C1-M8', 'Nom membre 8', 'Prénom membre 8', '1975-08-20', 'MALE', 'Lot UV 8 Ambato', 'Riziculteur',
        '0370234567', 'member.col1.8@fed-agri.mg', 'SENIOR', TRUE, TRUE, 'APPROVED', CURRENT_DATE - INTERVAL '9 months')
ON CONFLICT (id) DO NOTHING;

-- Membres - Collectivité 2
INSERT INTO member (id, last_name, first_name, birth_date, gender, address, profession, phone_number, email, occupation,
                    registration_fee_paid, membership_dues_paid, admission_status, join_date)
VALUES ('C2-M1', 'Nom membre 1', 'Prénom membre 1', '1980-02-01', 'MALE', 'Lot II V M Ambato', 'Riziculteur',
        '0341234567', 'member.col2.1@fed-agri.mg', 'SENIOR', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C2-M2', 'Nom membre 2', 'Prénom membre 2', '1982-03-05', 'MALE', 'Lot II F Ambato', 'Agriculteur',
        '0321234567', 'member.col2.2@fed-agri.mg', 'SENIOR', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C2-M3', 'Nom membre 3', 'Prénom membre 3', '1992-03-10', 'MALE', 'Lot II J Ambato', 'Collecteur', '0331234567',
        'member.col2.3@fed-agri.mg', 'SENIOR', TRUE, TRUE, 'APPROVED', CURRENT_DATE - INTERVAL '9 months'),
       ('C2-M4', 'Nom membre 4', 'Prénom membre 4', '1988-05-22', 'FEMALE', 'Lot A K 50 Ambato', 'Distributeur',
        '0381234567', 'member.col2.4@fed-agri.mg', 'SENIOR', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C2-M5', 'Nom membre 5', 'Prénom membre 5', '1999-08-21', 'MALE', 'Lot UV 80 Ambato', 'Riziculteur',
        '0373434567', 'member.col2.5@fed-agri.mg', 'PRESIDENT', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C2-M6', 'Nom membre 6', 'Prénom membre 6', '1998-08-22', 'FEMALE', 'Lot UV 6 Ambato', 'Riziculteur',
        '0372234567', 'member.col2.6@fed-agri.mg', 'VICE_PRESIDENT', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C2-M7', 'Nom membre 7', 'Prénom membre 7', '1998-01-31', 'MALE', 'Lot UV 7 Ambato', 'Riziculteur',
        '0374234567', 'member.col2.7@fed-agri.mg', 'SECRETARY', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C2-M8', 'Nom membre 8', 'Prénom membre 8', '1975-08-20', 'MALE', 'Lot UV 8 Ambato', 'Riziculteur',
        '0370234567', 'member.col2.8@fed-agri.mg', 'TREASURER', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months')
ON CONFLICT (id) DO NOTHING;

-- Membres - Collectivité 3
INSERT INTO member (id, last_name, first_name, birth_date, gender, address, profession, phone_number, email, occupation,
                    registration_fee_paid, membership_dues_paid, admission_status, join_date)
VALUES ('C3-M1', 'Nom membre 9', 'Prénom membre 9', '1988-01-02', 'MALE', 'Lot 33 J Antsirabe', 'Apiculteur',
        '034034567', 'member.col3.9@fed-agri.mg', 'PRESIDENT', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C3-M2', 'Nom membre 10', 'Prénom membre 10', '1982-03-05', 'MALE', 'Lot 2 J Antsirabe', 'Agriculteur',
        '0338634567', 'member.col3.10@fed-agri.mg', 'VICE_PRESIDENT', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C3-M3', 'Nom membre 11', 'Prénom membre 11', '1992-03-12', 'MALE', 'Lot 8 KM Antsirabe', 'Collecteur',
        '0338234567', 'member.col3.11@fed-agri.mg', 'SECRETARY', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C3-M4', 'Nom membre 12', 'Prénom membre 12', '1988-05-10', 'FEMALE', 'Lot A K 50 Antsirabe', 'Distributeur',
        '0382334567', 'member.col3.12@fed-agri.mg', 'TREASURER', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C3-M5', 'Nom membre 13', 'Prénom membre 13', '1999-08-11', 'MALE', 'Lot UV 80 Antsirabe', 'Apiculteur',
        '0373365567', 'member.col3.13@fed-agri.mg', 'SENIOR', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C3-M6', 'Nom membre 14', 'Prénom membre 14', '1998-08-09', 'FEMALE', 'Lot UV 6 Antsirabe', 'Apiculteur',
        '0378234567', 'member.col3.14@fed-agri.mg', 'SENIOR', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C3-M7', 'Nom membre 15', 'Prénom membre 15', '1998-01-13', 'MALE', 'Lot UV 7 Antsirabe', 'Apiculteur',
        '0374914567', 'member.col3.15@fed-agri.mg', 'SENIOR', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months'),
       ('C3-M8', 'Nom membre 16', 'Prénom membre 16', '1975-08-02', 'MALE', 'Lot UV 8 Antsirabe', 'Apiculteur',
        '0370634567', 'member.col3.16@fed-agri.mg', 'SENIOR', TRUE, TRUE, 'APPROVED',
        CURRENT_DATE - INTERVAL '9 months')
ON CONFLICT (id) DO NOTHING;

-- Adhésions (membership)
INSERT INTO membership (id, member_id, collectivity_id, joined_at)
VALUES ('ms-1-1', 'C1-M1', 'col-1', '2026-01-01'),
       ('ms-1-2', 'C1-M2', 'col-1', '2026-01-01'),
       ('ms-1-3', 'C1-M3', 'col-1', '2026-01-01'),
       ('ms-1-4', 'C1-M4', 'col-1', '2026-01-01'),
       ('ms-1-5', 'C1-M5', 'col-1', '2026-01-01'),
       ('ms-1-6', 'C1-M6', 'col-1', '2026-01-01'),
       ('ms-1-7', 'C1-M7', 'col-1', '2026-01-01'),
       ('ms-1-8', 'C1-M8', 'col-1', '2026-01-01'),
       ('ms-2-1', 'C2-M1', 'col-2', '2026-01-01'),
       ('ms-2-2', 'C2-M2', 'col-2', '2026-01-01'),
       ('ms-2-3', 'C2-M3', 'col-2', '2026-01-01'),
       ('ms-2-4', 'C2-M4', 'col-2', '2026-01-01'),
       ('ms-2-5', 'C2-M5', 'col-2', '2026-01-01'),
       ('ms-2-6', 'C2-M6', 'col-2', '2026-01-01'),
       ('ms-2-7', 'C2-M7', 'col-2', '2026-01-01'),
       ('ms-2-8', 'C2-M8', 'col-2', '2026-01-01'),
       ('ms-3-1', 'C3-M1', 'col-3', '2026-01-01'),
       ('ms-3-2', 'C3-M2', 'col-3', '2026-01-01'),
       ('ms-3-3', 'C3-M3', 'col-3', '2026-01-01'),
       ('ms-3-4', 'C3-M4', 'col-3', '2026-01-01'),
       ('ms-3-5', 'C3-M5', 'col-3', '2026-01-01'),
       ('ms-3-6', 'C3-M6', 'col-3', '2026-01-01'),
       ('ms-3-7', 'C3-M7', 'col-3', '2026-01-01'),
       ('ms-3-8', 'C3-M8', 'col-3', '2026-01-01')
ON CONFLICT (id) DO NOTHING;

-- Attribution des rôles (assignment)
INSERT INTO assignment (id, member_id, role_id, mandate_id, collectivity_id)
VALUES ('as-1-1', 'C1-M1', (SELECT id FROM role WHERE name = 'PRESIDENT'), 'man-1', 'col-1'),
       ('as-1-2', 'C1-M2', (SELECT id FROM role WHERE name = 'VICE_PRESIDENT'), 'man-1', 'col-1'),
       ('as-1-3', 'C1-M3', (SELECT id FROM role WHERE name = 'SECRETARY'), 'man-1', 'col-1'),
       ('as-1-4', 'C1-M4', (SELECT id FROM role WHERE name = 'TREASURER'), 'man-1', 'col-1'),
       ('as-2-1', 'C2-M5', (SELECT id FROM role WHERE name = 'PRESIDENT'), 'man-1', 'col-2'),
       ('as-2-2', 'C2-M6', (SELECT id FROM role WHERE name = 'VICE_PRESIDENT'), 'man-1', 'col-2'),
       ('as-2-3', 'C2-M7', (SELECT id FROM role WHERE name = 'SECRETARY'), 'man-1', 'col-2'),
       ('as-2-4', 'C2-M8', (SELECT id FROM role WHERE name = 'TREASURER'), 'man-1', 'col-2'),
       ('as-3-1', 'C3-M1', (SELECT id FROM role WHERE name = 'PRESIDENT'), 'man-1', 'col-3'),
       ('as-3-2', 'C3-M2', (SELECT id FROM role WHERE name = 'VICE_PRESIDENT'), 'man-1', 'col-3'),
       ('as-3-3', 'C3-M3', (SELECT id FROM role WHERE name = 'SECRETARY'), 'man-1', 'col-3'),
       ('as-3-4', 'C3-M4', (SELECT id FROM role WHERE name = 'TREASURER'), 'man-1', 'col-3')
ON CONFLICT (id) DO NOTHING;

-- Parrainages (sponsorship) - Collectivité 1
INSERT INTO sponsorship (id, member_id, sponsor_member_id, sponsor_collectivity_id, relationship)
VALUES ('sp-1-1', 'C1-M3', 'C1-M1', 'col-1', 'OTHER'),
       ('sp-1-2', 'C1-M3', 'C1-M2', 'col-1', 'OTHER'),
       ('sp-1-3', 'C1-M4', 'C1-M1', 'col-1', 'OTHER'),
       ('sp-1-4', 'C1-M4', 'C1-M2', 'col-1', 'OTHER'),
       ('sp-1-5', 'C1-M5', 'C1-M1', 'col-1', 'OTHER'),
       ('sp-1-6', 'C1-M5', 'C1-M2', 'col-1', 'OTHER'),
       ('sp-1-7', 'C1-M6', 'C1-M1', 'col-1', 'OTHER'),
       ('sp-1-8', 'C1-M6', 'C1-M2', 'col-1', 'OTHER'),
       ('sp-1-9', 'C1-M7', 'C1-M1', 'col-1', 'OTHER'),
       ('sp-1-10', 'C1-M7', 'C1-M2', 'col-1', 'OTHER'),
       ('sp-1-11', 'C1-M8', 'C1-M6', 'col-1', 'OTHER'),
       ('sp-1-12', 'C1-M8', 'C1-M7', 'col-1', 'OTHER')
ON CONFLICT (id) DO NOTHING;

-- Parrainages - Collectivité 2
INSERT INTO sponsorship (id, member_id, sponsor_member_id, sponsor_collectivity_id, relationship)
VALUES ('sp-2-1', 'C2-M3', 'C1-M1', 'col-1', 'OTHER'),
       ('sp-2-2', 'C2-M3', 'C1-M2', 'col-1', 'OTHER'),
       ('sp-2-3', 'C2-M4', 'C1-M1', 'col-1', 'OTHER'),
       ('sp-2-4', 'C2-M4', 'C1-M2', 'col-1', 'OTHER'),
       ('sp-2-5', 'C2-M5', 'C1-M1', 'col-1', 'OTHER'),
       ('sp-2-6', 'C2-M5', 'C1-M2', 'col-1', 'OTHER'),
       ('sp-2-7', 'C2-M6', 'C1-M1', 'col-1', 'OTHER'),
       ('sp-2-8', 'C2-M6', 'C1-M2', 'col-1', 'OTHER'),
       ('sp-2-9', 'C2-M7', 'C1-M1', 'col-1', 'OTHER'),
       ('sp-2-10', 'C2-M7', 'C1-M2', 'col-1', 'OTHER'),
       ('sp-2-11', 'C2-M8', 'C1-M6', 'col-1', 'OTHER'),
       ('sp-2-12', 'C2-M8', 'C1-M7', 'col-1', 'OTHER')
ON CONFLICT (id) DO NOTHING;

-- Parrainages - Collectivité 3
INSERT INTO sponsorship (id, member_id, sponsor_member_id, sponsor_collectivity_id, relationship)
VALUES ('sp-3-1', 'C3-M3', 'C3-M1', 'col-3', 'OTHER'),
       ('sp-3-2', 'C3-M3', 'C3-M2', 'col-3', 'OTHER'),
       ('sp-3-3', 'C3-M4', 'C3-M1', 'col-3', 'OTHER'),
       ('sp-3-4', 'C3-M4', 'C3-M2', 'col-3', 'OTHER'),
       ('sp-3-5', 'C3-M5', 'C3-M1', 'col-3', 'OTHER'),
       ('sp-3-6', 'C3-M5', 'C3-M2', 'col-3', 'OTHER'),
       ('sp-3-7', 'C3-M6', 'C3-M1', 'col-3', 'OTHER'),
       ('sp-3-8', 'C3-M6', 'C3-M2', 'col-3', 'OTHER'),
       ('sp-3-9', 'C3-M7', 'C3-M1', 'col-3', 'OTHER'),
       ('sp-3-10', 'C3-M7', 'C3-M2', 'col-3', 'OTHER'),
       ('sp-3-11', 'C3-M8', 'C3-M1', 'col-3', 'OTHER'),
       ('sp-3-12', 'C3-M8', 'C3-M2', 'col-3', 'OTHER')
ON CONFLICT (id) DO NOTHING;

-- Cotisations
INSERT INTO membership_fee (id, collectivity_id, eligible_from, frequency, amount, label, status)
VALUES ('cot-1', 'col-1', '2026-01-01', 'ANNUALLY', 100000, 'Cotisation annuelle', 'ACTIVE'),
       ('cot-2', 'col-2', '2026-01-01', 'ANNUALLY', 100000, 'Cotisation annuelle', 'ACTIVE'),
       ('cot-3', 'col-3', '2026-01-01', 'ANNUALLY', 50000, 'Cotisation annuelle', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- Comptes financiers
INSERT INTO financial_account (id, collectivity_id, account_type, holder_name, mobile_number, balance)
VALUES ('C1-A-CASH', 'col-1', 'CASH', NULL, NULL, 740000),
       ('C1-A-MOBILE-1', 'col-1', 'MOBILE_BANKING', 'Mpanorina', '0370489612', 0),
       ('C2-A-CASH', 'col-2', 'CASH', NULL, NULL, 650000),
       ('C2-A-MOBILE-1', 'col-2', 'MOBILE_BANKING', 'Dobo voalohany', '0320489612', 100000),
       ('C3-A-CASH', 'col-3', 'CASH', NULL, NULL, 0)
ON CONFLICT (id) DO NOTHING;

-- Paiements - Collectivité 1
INSERT INTO member_payment (id, member_id, membership_fee_id, financial_account_id, amount, payment_mode, payment_date,
                            status)
VALUES ('mp-1-1', 'C1-M1', 'cot-1', 'C1-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('mp-1-2', 'C1-M2', 'cot-1', 'C1-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('mp-1-3', 'C1-M3', 'cot-1', 'C1-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('mp-1-4', 'C1-M4', 'cot-1', 'C1-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('mp-1-5', 'C1-M5', 'cot-1', 'C1-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('mp-1-6', 'C1-M6', 'cot-1', 'C1-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('mp-1-7', 'C1-M7', 'cot-1', 'C1-A-CASH', 60000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('mp-1-8', 'C1-M8', 'cot-1', 'C1-A-CASH', 90000, 'CASH', '2026-01-01', 'COMPLETED')
ON CONFLICT (id) DO NOTHING;

-- Paiements - Collectivité 2
INSERT INTO member_payment (id, member_id, membership_fee_id, financial_account_id, amount, payment_mode, payment_date,
                            status)
VALUES ('mp-2-1', 'C2-M1', 'cot-2', 'C2-A-CASH', 60000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('mp-2-2', 'C2-M2', 'cot-2', 'C2-A-CASH', 90000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('mp-2-3', 'C2-M3', 'cot-2', 'C2-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('mp-2-4', 'C2-M4', 'cot-2', 'C2-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('mp-2-5', 'C2-M5', 'cot-2', 'C2-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('mp-2-6', 'C2-M6', 'cot-2', 'C2-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('mp-2-7', 'C2-M7', 'cot-2', 'C2-A-MOBILE-1', 40000, 'MOBILE_BANKING', '2026-01-01', 'COMPLETED'),
       ('mp-2-8', 'C2-M8', 'cot-2', 'C2-A-MOBILE-1', 60000, 'MOBILE_BANKING', '2026-01-01', 'COMPLETED')
ON CONFLICT (id) DO NOTHING;

-- Transactions - Collectivité 1
INSERT INTO collectivity_transaction (id, collectivity_id, member_id, membership_fee_id, financial_account_id, amount,
                                      payment_mode, payment_date, status)
VALUES ('tx-1-1', 'col-1', 'C1-M1', 'cot-1', 'C1-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('tx-1-2', 'col-1', 'C1-M2', 'cot-1', 'C1-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('tx-1-3', 'col-1', 'C1-M3', 'cot-1', 'C1-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('tx-1-4', 'col-1', 'C1-M4', 'cot-1', 'C1-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('tx-1-5', 'col-1', 'C1-M5', 'cot-1', 'C1-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('tx-1-6', 'col-1', 'C1-M6', 'cot-1', 'C1-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('tx-1-7', 'col-1', 'C1-M7', 'cot-1', 'C1-A-CASH', 60000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('tx-1-8', 'col-1', 'C1-M8', 'cot-1', 'C1-A-CASH', 90000, 'CASH', '2026-01-01', 'COMPLETED')
ON CONFLICT (id) DO NOTHING;

-- Transactions - Collectivité 2
INSERT INTO collectivity_transaction (id, collectivity_id, member_id, membership_fee_id, financial_account_id, amount,
                                      payment_mode, payment_date, status)
VALUES ('tx-2-1', 'col-2', 'C2-M1', 'cot-2', 'C2-A-CASH', 60000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('tx-2-2', 'col-2', 'C2-M2', 'cot-2', 'C2-A-CASH', 90000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('tx-2-3', 'col-2', 'C2-M3', 'cot-2', 'C2-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('tx-2-4', 'col-2', 'C2-M4', 'cot-2', 'C2-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('tx-2-5', 'col-2', 'C2-M5', 'cot-2', 'C2-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('tx-2-6', 'col-2', 'C2-M6', 'cot-2', 'C2-A-CASH', 100000, 'CASH', '2026-01-01', 'COMPLETED'),
       ('tx-2-7', 'col-2', 'C2-M7', 'cot-2', 'C2-A-MOBILE-1', 40000, 'MOBILE_BANKING', '2026-01-01', 'COMPLETED'),
       ('tx-2-8', 'col-2', 'C2-M8', 'cot-2', 'C2-A-MOBILE-1', 60000, 'MOBILE_BANKING', '2026-01-01', 'COMPLETED')
ON CONFLICT (id) DO NOTHING;

ALTER TABLE activity ALTER COLUMN member_occupation_concerned TYPE VARCHAR(255);

-- =====================================================
-- FIN DU SCRIPT
-- =====================================================