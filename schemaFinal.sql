-- =====================================================
-- SCRIPT DE CRÉATION DES TABLES ET INSERTION DES DONNÉES
-- AGRICULTURAL FEDERATION API - DONNÉES TEST 6 MAI 2026
-- =====================================================

-- =====================================================
-- 1. SUPPRESSION DES TABLES EXISTANTES
-- =====================================================
DROP TABLE IF EXISTS activity_member_attendance CASCADE;
DROP TABLE IF EXISTS collectivity_activity CASCADE;
DROP TABLE IF EXISTS activity_member_occupation_concerned CASCADE;
DROP TABLE IF EXISTS member_payment CASCADE;
DROP TABLE IF EXISTS membership_fee_installment CASCADE;
DROP TABLE IF EXISTS collectivity_membership_fee CASCADE;
DROP TABLE IF EXISTS membership_fee CASCADE;
DROP TABLE IF EXISTS member_referee CASCADE;
DROP TABLE IF EXISTS collectivity_member CASCADE;
DROP TABLE IF EXISTS financial_account CASCADE;
DROP TABLE IF EXISTS activity CASCADE;
DROP TABLE IF EXISTS "member" CASCADE;
DROP TABLE IF EXISTS "collectivity" CASCADE;

-- =====================================================
-- 2. CRÉATION DES TYPES ENUM
-- =====================================================
DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'gender') THEN
            CREATE TYPE gender AS ENUM ('MALE', 'FEMALE');
        END IF;
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'member_occupation') THEN
            CREATE TYPE member_occupation AS ENUM ('JUNIOR', 'SENIOR', 'SECRETARY', 'TREASURER', 'VICE_PRESIDENT', 'PRESIDENT');
        END IF;
    END
$$;

-- =====================================================
-- 3. CRÉATION DE LA TABLE COLLECTIVITY
-- =====================================================
CREATE TABLE "collectivity"
(
    id                VARCHAR PRIMARY KEY,
    name              VARCHAR,
    number            INTEGER,
    location          VARCHAR,
    specialization    VARCHAR,
    president_id      VARCHAR,
    vice_president_id VARCHAR,
    treasurer_id      VARCHAR,
    secretary_id      VARCHAR
);

-- =====================================================
-- 4. CRÉATION DE LA TABLE MEMBER
-- =====================================================
CREATE TABLE "member"
(
    id                    VARCHAR PRIMARY KEY,
    first_name            VARCHAR,
    last_name             VARCHAR,
    birth_date            DATE,
    gender                gender,
    address               VARCHAR,
    profession            VARCHAR,
    phone_number          VARCHAR,
    email                 VARCHAR,
    occupation            member_occupation,
    registration_fee_paid BOOLEAN,
    membership_dues_paid  BOOLEAN,
    created_at            DATE DEFAULT CURRENT_DATE
);

-- =====================================================
-- 5. CRÉATION DE LA TABLE COLLECTIVITY_MEMBER
-- =====================================================
CREATE TABLE "collectivity_member"
(
    id              VARCHAR PRIMARY KEY,
    member_id       VARCHAR REFERENCES "member" (id),
    collectivity_id VARCHAR REFERENCES "collectivity" (id)
);

-- =====================================================
-- 6. CRÉATION DE LA TABLE MEMBER_REFEREE
-- =====================================================
CREATE TABLE "member_referee"
(
    id                 VARCHAR PRIMARY KEY,
    member_refereed_id VARCHAR REFERENCES "member" (id),
    member_referee_id  VARCHAR REFERENCES "member" (id)
);

-- =====================================================
-- 7. CRÉATION DE LA TABLE MEMBERSHIP_FEE
-- =====================================================
CREATE TABLE membership_fee
(
    id            VARCHAR PRIMARY KEY,
    label         VARCHAR,
    status        VARCHAR CHECK (status IN ('ACTIVE', 'INACTIVE')),
    frequency     VARCHAR CHECK (frequency IN ('WEEKLY', 'MONTHLY', 'ANNUALLY', 'PUNCTUALLY')),
    eligible_from DATE,
    amount        DECIMAL
);

-- =====================================================
-- 8. CRÉATION DE LA TABLE COLLECTIVITY_MEMBERSHIP_FEE
-- =====================================================
CREATE TABLE collectivity_membership_fee
(
    id                VARCHAR PRIMARY KEY,
    collectivity_id   VARCHAR REFERENCES "collectivity" (id),
    membership_fee_id VARCHAR REFERENCES membership_fee (id)
);

-- =====================================================
-- 9. CRÉATION DE LA TABLE FINANCIAL_ACCOUNT
-- =====================================================
CREATE TABLE financial_account
(
    id              VARCHAR PRIMARY KEY,
    collectivity_id VARCHAR NOT NULL REFERENCES "collectivity" (id),
    account_type    VARCHAR NOT NULL CHECK (account_type IN ('CASH', 'BANK', 'ORANGE_MONEY', 'MVOLA', 'AIRTEL_MONEY')),
    holder_name     VARCHAR,
    phone_number    VARCHAR,
    bank_name       VARCHAR,
    bank_code       VARCHAR,
    agency_code     VARCHAR,
    account_number  VARCHAR,
    rib_key         VARCHAR,
    balance         DECIMAL DEFAULT 0,
    created_at      DATE    DEFAULT CURRENT_DATE
);

-- =====================================================
-- 10. CRÉATION DE LA TABLE MEMBER_PAYMENT
-- =====================================================
CREATE TABLE member_payment
(
    id                  VARCHAR PRIMARY KEY,
    member_id           VARCHAR REFERENCES "member" (id),
    membership_fee_id   VARCHAR REFERENCES membership_fee (id),
    amount              DECIMAL,
    payment_mode        VARCHAR CHECK (payment_mode IN ('CASH', 'MOBILE_BANKING', 'BANK_TRANSFER')),
    creation_date       DATE,
    account_credited_id VARCHAR
);

-- =====================================================
-- 11. INSERTION DES COLLECTIVITÉS
-- =====================================================
INSERT INTO "collectivity" (id, name, number, location, specialization)
VALUES ('col-1', 'Mpanorina', 1, 'Ambatondrazaka', 'Riziculture'),
       ('col-2', 'Dobo voalohany', 2, 'Ambatondrazaka', 'Pisciculture'),
       ('col-3', 'Tantely mamy', 3, 'Brickaville', 'Apiculture');

-- =====================================================
-- 12. INSERTION DES ANCIENS MEMBRES (adhésion 01/01/2026)
-- =====================================================
INSERT INTO "member" (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email,
                      occupation, registration_fee_paid, membership_dues_paid, created_at)
VALUES ('C1-M1', 'Prénom membre 1', 'Nom membre 1', '1980-02-01', 'MALE', 'Lot II V M Ambato.', 'Riziculteur',
        '0341234567', 'member.1@fed-agri.mg', 'PRESIDENT', true, true, '2026-01-01'),
       ('C1-M2', 'Prénom membre 2', 'Nom membre 2', '1982-03-05', 'MALE', 'Lot II F Ambato.', 'Agriculteur',
        '0321234567', 'member.2@fed-agri.mg', 'VICE_PRESIDENT', true, true, '2026-01-01'),
       ('C1-M3', 'Prénom membre 3', 'Nom membre 3', '1992-03-10', 'MALE', 'Lot II J Ambato.', 'Collecteur',
        '0331234567', 'member.3@fed-agri.mg', 'SECRETARY', true, true, '2026-01-01'),
       ('C1-M4', 'Prénom membre 4', 'Nom membre 4', '1988-05-22', 'FEMALE', 'Lot A K 50 Ambato.', 'Distributeur',
        '0381234567', 'member.4@fed-agri.mg', 'TREASURER', true, true, '2026-01-01'),
       ('C1-M5', 'Prénom membre 5', 'Nom membre 5', '1999-08-21', 'MALE', 'Lot UV 80 Ambato.', 'Riziculteur',
        '0373434567', 'member.5@fed-agri.mg', 'SENIOR', true, true, '2026-01-01'),
       ('C1-M6', 'Prénom membre 6', 'Nom membre 6', '1998-08-22', 'FEMALE', 'Lot UV 6 Ambato.', 'Riziculteur',
        '0372234567', 'member.6@fed-agri.mg', 'SENIOR', true, true, '2026-01-01'),
       ('C1-M7', 'Prénom membre 7', 'Nom membre 7', '1998-01-31', 'MALE', 'Lot UV 7 Ambato.', 'Riziculteur',
        '0374234567', 'member.7@fed-agri.mg', 'SENIOR', true, true, '2026-01-01'),
       ('C1-M8', 'Prénom membre 8', 'Nom membre 8', '1975-08-20', 'MALE', 'Lot UV 8 Ambato.', 'Riziculteur',
        '0370234567', 'member.8@fed-agri.mg', 'SENIOR', true, true, '2026-01-01'),
       ('C2-M1', 'Prénom membre 1', 'Nom membre 1', '1980-02-01', 'MALE', 'Lot II V M Ambato.', 'Riziculteur',
        '0341234567', 'member.1@fed-agri.mg', 'SENIOR', true, true, '2026-01-01'),
       ('C2-M2', 'Prénom membre 2', 'Nom membre 2', '1982-03-05', 'MALE', 'Lot II F Ambato.', 'Agriculteur',
        '0321234567', 'member.2@fed-agri.mg', 'SENIOR', true, true, '2026-01-01'),
       ('C2-M3', 'Prénom membre 3', 'Nom membre 3', '1992-03-10', 'MALE', 'Lot II J Ambato.', 'Collecteur',
        '0331234567', 'member.3@fed-agri.mg', 'SENIOR', true, true, '2026-01-01'),
       ('C2-M4', 'Prénom membre 4', 'Nom membre 4', '1988-05-22', 'FEMALE', 'Lot A K 50 Ambato.', 'Distributeur',
        '0381234567', 'member.4@fed-agri.mg', 'SENIOR', true, true, '2026-01-01'),
       ('C2-M5', 'Prénom membre 5', 'Nom membre 5', '1999-08-21', 'MALE', 'Lot UV 80 Ambato.', 'Riziculteur',
        '0373434567', 'member.5@fed-agri.mg', 'PRESIDENT', true, true, '2026-01-01'),
       ('C2-M6', 'Prénom membre 6', 'Nom membre 6', '1998-08-22', 'FEMALE', 'Lot UV 6 Ambato.', 'Riziculteur',
        '0372234567', 'member.6@fed-agri.mg', 'VICE_PRESIDENT', true, true, '2026-01-01'),
       ('C2-M7', 'Prénom membre 7', 'Nom membre 7', '1998-01-31', 'MALE', 'Lot UV 7 Ambato.', 'Riziculteur',
        '0374234567', 'member.7@fed-agri.mg', 'SECRETARY', true, true, '2026-01-01'),
       ('C2-M8', 'Prénom membre 8', 'Nom membre 8', '1975-08-20', 'MALE', 'Lot UV 8 Ambato.', 'Riziculteur',
        '0370234567', 'member.8@fed-agri.mg', 'TREASURER', true, true, '2026-01-01'),
       ('C3-M1', 'Prénom membre 9', 'Nom membre 9', '1988-01-02', 'MALE', 'Lot 33 J Antsirabe', 'Apiculteur',
        '0340345670', 'member.9@fed-agri.mg', 'PRESIDENT', true, true, '2026-01-01'),
       ('C3-M2', 'Prénom membre 10', 'Nom membre 10', '1982-03-05', 'MALE', 'Lot 2 J Antsirabe', 'Agriculteur',
        '0338634567', 'member.10@fed-agri.mg', 'VICE_PRESIDENT', true, true, '2026-01-01'),
       ('C3-M3', 'Prénom membre 11', 'Nom membre 11', '1992-03-12', 'MALE', 'Lot 8 KM Antsirabe', 'Collecteur',
        '0338234567', 'member.11@fed-agri.mg', 'SECRETARY', true, true, '2026-01-01'),
       ('C3-M4', 'Prénom membre 12', 'Nom membre 12', '1988-05-10', 'FEMALE', 'Lot A K 50 Antsirabe', 'Distributeur',
        '0382334567', 'member.12@fed-agri.mg', 'TREASURER', true, true, '2026-01-01'),
       ('C3-M5', 'Prénom membre 13', 'Nom membre 13', '1999-08-11', 'MALE', 'Lot UV 80 Antsirabe.', 'Apiculteur',
        '0373365567', 'member.13@fed-agri.mg', 'SENIOR', true, true, '2026-01-01'),
       ('C3-M6', 'Prénom membre 14', 'Nom membre 14', '1998-08-09', 'FEMALE', 'Lot UV 6 Antsirabe.', 'Apiculteur',
        '0378234567', 'member.14@fed-agri.mg', 'SENIOR', true, true, '2026-01-01'),
       ('C3-M7', 'Prénom membre 15', 'Nom membre 15', '1998-01-13', 'MALE', 'Lot UV 7 Antsirabe', 'Apiculteur',
        '0374914567', 'member.15@fed-agri.mg', 'SENIOR', true, true, '2026-01-01'),
       ('C3-M8', 'Prénom membre 16', 'Nom membre 16', '1975-08-02', 'MALE', 'Lot UV 8 Antsirabe', 'Apiculteur',
        '0370634567', 'member.16@fed-agri.mg', 'SENIOR', true, true, '2026-01-01');

-- =====================================================
-- 13. INSERTION DES NOUVEAUX MEMBRES (adhésion variable, AUCUN paiement)
-- =====================================================
INSERT INTO "member" (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email,
                      occupation, registration_fee_paid, membership_dues_paid, created_at)
VALUES ('C1-M-NEW-1', 'Prénom nouveau 1', 'Nom nouveau 1', '2000-05-15', 'MALE', 'Adresse nouvelle 1',
        'Métier nouveau 1', '0340000001', 'new.c1.member.1@fed-agri.mg', 'JUNIOR', true, false, '2026-04-01'),
       ('C1-M-NEW-2', 'Prénom nouveau 2', 'Nom nouveau 2', '2000-06-20', 'FEMALE', 'Adresse nouvelle 2',
        'Métier nouveau 2', '0340000002', 'new.c1.member.2@fed-agri.mg', 'JUNIOR', true, false, '2026-04-01'),
       ('C1-M-NEW-3', 'Prénom nouveau 3', 'Nom nouveau 3', '2001-07-10', 'MALE', 'Adresse nouvelle 3',
        'Métier nouveau 3', '0340000003', 'new.c1.member.3@fed-agri.mg', 'JUNIOR', true, false, '2026-05-01'),
       ('C1-M-NEW-4', 'Prénom nouveau 4', 'Nom nouveau 4', '2000-08-25', 'FEMALE', 'Adresse nouvelle 4',
        'Métier nouveau 4', '0340000004', 'new.c1.member.4@fed-agri.mg', 'JUNIOR', true, false, '2026-06-01'),
       ('C2-M-NEW-1', 'Prénom nouveau 1', 'Nom nouveau 1', '2000-05-15', 'MALE', 'Adresse nouvelle 1',
        'Métier nouveau 1', '0340000011', 'new.c2.member.1@fed-agri.mg', 'JUNIOR', true, false, '2026-03-01'),
       ('C2-M-NEW-2', 'Prénom nouveau 2', 'Nom nouveau 2', '2000-06-20', 'FEMALE', 'Adresse nouvelle 2',
        'Métier nouveau 2', '0340000012', 'new.c2.member.2@fed-agri.mg', 'JUNIOR', true, false, '2026-03-01'),
       ('C2-M-NEW-3', 'Prénom nouveau 3', 'Nom nouveau 3', '2001-07-10', 'MALE', 'Adresse nouvelle 3',
        'Métier nouveau 3', '0340000013', 'new.c2.member.3@fed-agri.mg', 'JUNIOR', true, false, '2026-03-01'),
       ('C3-M-NEW-1', 'Prénom nouveau 1', 'Nom nouveau 1', '2000-05-15', 'MALE', 'Adresse nouvelle 1',
        'Métier nouveau 1', '0340000021', 'new.c3.member.1@fed-agri.mg', 'JUNIOR', true, false, '2026-01-01'),
       ('C3-M-NEW-2', 'Prénom nouveau 2', 'Nom nouveau 2', '2000-06-20', 'FEMALE', 'Adresse nouvelle 2',
        'Métier nouveau 2', '0340000022', 'new.c3.member.2@fed-agri.mg', 'JUNIOR', true, false, '2026-02-01'),
       ('C3-M-NEW-3', 'Prénom nouveau 3', 'Nom nouveau 3', '2001-07-10', 'MALE', 'Adresse nouvelle 3',
        'Métier nouveau 3', '0340000023', 'new.c3.member.3@fed-agri.mg', 'JUNIOR', true, false, '2026-02-01'),
       ('C3-M-NEW-4', 'Prénom nouveau 4', 'Nom nouveau 4', '2000-08-25', 'FEMALE', 'Adresse nouvelle 4',
        'Métier nouveau 4', '0340000024', 'new.c3.member.4@fed-agri.mg', 'JUNIOR', true, false, '2026-03-01'),
       ('C3-M-NEW-5', 'Prénom nouveau 5', 'Nom nouveau 5', '2001-09-30', 'MALE', 'Adresse nouvelle 5',
        'Métier nouveau 5', '0340000025', 'new.c3.member.5@fed-agri.mg', 'JUNIOR', true, false, '2026-03-01'),
       ('C3-M-NEW-6', 'Prénom nouveau 6', 'Nom nouveau 6', '2000-10-12', 'FEMALE', 'Adresse nouvelle 6',
        'Métier nouveau 6', '0340000026', 'new.c3.member.6@fed-agri.mg', 'JUNIOR', true, false, '2026-03-01');

-- =====================================================
-- 14. INSERTION DES LIENS COLLECTIVITÉ-MEMBRE
-- =====================================================
INSERT INTO "collectivity_member" (id, member_id, collectivity_id)
VALUES ('cm-1', 'C1-M1', 'col-1'),
       ('cm-2', 'C1-M2', 'col-1'),
       ('cm-3', 'C1-M3', 'col-1'),
       ('cm-4', 'C1-M4', 'col-1'),
       ('cm-5', 'C1-M5', 'col-1'),
       ('cm-6', 'C1-M6', 'col-1'),
       ('cm-7', 'C1-M7', 'col-1'),
       ('cm-8', 'C1-M8', 'col-1'),
       ('cm-c1-new-1', 'C1-M-NEW-1', 'col-1'),
       ('cm-c1-new-2', 'C1-M-NEW-2', 'col-1'),
       ('cm-c1-new-3', 'C1-M-NEW-3', 'col-1'),
       ('cm-c1-new-4', 'C1-M-NEW-4', 'col-1'),
       ('cm-9', 'C2-M1', 'col-2'),
       ('cm-10', 'C2-M2', 'col-2'),
       ('cm-11', 'C2-M3', 'col-2'),
       ('cm-12', 'C2-M4', 'col-2'),
       ('cm-13', 'C2-M5', 'col-2'),
       ('cm-14', 'C2-M6', 'col-2'),
       ('cm-15', 'C2-M7', 'col-2'),
       ('cm-16', 'C2-M8', 'col-2'),
       ('cm-c2-new-1', 'C2-M-NEW-1', 'col-2'),
       ('cm-c2-new-2', 'C2-M-NEW-2', 'col-2'),
       ('cm-c2-new-3', 'C2-M-NEW-3', 'col-2'),
       ('cm-17', 'C3-M1', 'col-3'),
       ('cm-18', 'C3-M2', 'col-3'),
       ('cm-19', 'C3-M3', 'col-3'),
       ('cm-20', 'C3-M4', 'col-3'),
       ('cm-21', 'C3-M5', 'col-3'),
       ('cm-22', 'C3-M6', 'col-3'),
       ('cm-23', 'C3-M7', 'col-3'),
       ('cm-24', 'C3-M8', 'col-3'),
       ('cm-c3-new-1', 'C3-M-NEW-1', 'col-3'),
       ('cm-c3-new-2', 'C3-M-NEW-2', 'col-3'),
       ('cm-c3-new-3', 'C3-M-NEW-3', 'col-3'),
       ('cm-c3-new-4', 'C3-M-NEW-4', 'col-3'),
       ('cm-c3-new-5', 'C3-M-NEW-5', 'col-3'),
       ('cm-c3-new-6', 'C3-M-NEW-6', 'col-3');

-- =====================================================
-- 15. INSERTION DES RÉFÉRANTS
-- =====================================================
INSERT INTO "member_referee" (id, member_refereed_id, member_referee_id)
VALUES ('ref-1', 'C1-M3', 'C1-M1'),
       ('ref-2', 'C1-M3', 'C1-M2'),
       ('ref-3', 'C1-M4', 'C1-M1'),
       ('ref-4', 'C1-M4', 'C1-M2'),
       ('ref-5', 'C1-M5', 'C1-M1'),
       ('ref-6', 'C1-M5', 'C1-M2'),
       ('ref-7', 'C1-M6', 'C1-M1'),
       ('ref-8', 'C1-M6', 'C1-M2'),
       ('ref-9', 'C1-M7', 'C1-M1'),
       ('ref-10', 'C1-M7', 'C1-M2'),
       ('ref-11', 'C1-M8', 'C1-M6'),
       ('ref-12', 'C1-M8', 'C1-M7'),
       ('ref-c1-new-1', 'C1-M-NEW-1', 'C1-M1'),
       ('ref-c1-new-2', 'C1-M-NEW-1', 'C1-M2'),
       ('ref-c1-new-3', 'C1-M-NEW-2', 'C1-M1'),
       ('ref-c1-new-4', 'C1-M-NEW-2', 'C1-M2'),
       ('ref-c1-new-5', 'C1-M-NEW-3', 'C1-M1'),
       ('ref-c1-new-6', 'C1-M-NEW-3', 'C1-M2'),
       ('ref-c1-new-7', 'C1-M-NEW-4', 'C1-M1'),
       ('ref-c1-new-8', 'C1-M-NEW-4', 'C1-M2'),
       ('ref-13', 'C2-M3', 'C1-M1'),
       ('ref-14', 'C2-M3', 'C1-M2'),
       ('ref-15', 'C2-M4', 'C1-M1'),
       ('ref-16', 'C2-M4', 'C1-M2'),
       ('ref-17', 'C2-M5', 'C1-M1'),
       ('ref-18', 'C2-M5', 'C1-M2'),
       ('ref-19', 'C2-M6', 'C1-M1'),
       ('ref-20', 'C2-M6', 'C1-M2'),
       ('ref-21', 'C2-M7', 'C1-M1'),
       ('ref-22', 'C2-M7', 'C1-M2'),
       ('ref-23', 'C2-M8', 'C1-M6'),
       ('ref-24', 'C2-M8', 'C1-M7'),
       ('ref-c2-new-1', 'C2-M-NEW-1', 'C1-M1'),
       ('ref-c2-new-2', 'C2-M-NEW-1', 'C1-M2'),
       ('ref-c2-new-3', 'C2-M-NEW-2', 'C1-M1'),
       ('ref-c2-new-4', 'C2-M-NEW-2', 'C1-M2'),
       ('ref-c2-new-5', 'C2-M-NEW-3', 'C1-M1'),
       ('ref-c2-new-6', 'C2-M-NEW-3', 'C1-M2'),
       ('ref-25', 'C3-M3', 'C3-M1'),
       ('ref-26', 'C3-M3', 'C3-M2'),
       ('ref-27', 'C3-M4', 'C3-M1'),
       ('ref-28', 'C3-M4', 'C3-M2'),
       ('ref-29', 'C3-M5', 'C3-M1'),
       ('ref-30', 'C3-M5', 'C3-M2'),
       ('ref-31', 'C3-M6', 'C3-M1'),
       ('ref-32', 'C3-M6', 'C3-M2'),
       ('ref-33', 'C3-M7', 'C3-M1'),
       ('ref-34', 'C3-M7', 'C3-M2'),
       ('ref-35', 'C3-M8', 'C3-M1'),
       ('ref-36', 'C3-M8', 'C3-M2'),
       ('ref-c3-new-1', 'C3-M-NEW-1', 'C3-M1'),
       ('ref-c3-new-2', 'C3-M-NEW-1', 'C3-M2'),
       ('ref-c3-new-3', 'C3-M-NEW-2', 'C3-M1'),
       ('ref-c3-new-4', 'C3-M-NEW-2', 'C3-M2'),
       ('ref-c3-new-5', 'C3-M-NEW-3', 'C3-M1'),
       ('ref-c3-new-6', 'C3-M-NEW-3', 'C3-M2'),
       ('ref-c3-new-7', 'C3-M-NEW-4', 'C3-M1'),
       ('ref-c3-new-8', 'C3-M-NEW-4', 'C3-M2'),
       ('ref-c3-new-9', 'C3-M-NEW-5', 'C3-M1'),
       ('ref-c3-new-10', 'C3-M-NEW-5', 'C3-M2'),
       ('ref-c3-new-11', 'C3-M-NEW-6', 'C3-M1'),
       ('ref-c3-new-12', 'C3-M-NEW-6', 'C3-M2');

-- =====================================================
-- 16. INSERTION DES COMPTES FINANCIERS
-- =====================================================
INSERT INTO financial_account (id, collectivity_id, account_type, holder_name, phone_number, balance)
VALUES ('C1-A-CASH', 'col-1', 'CASH', NULL, NULL, 0),
       ('C1-A-MOBILE-1', 'col-1', 'ORANGE_MONEY', 'Mpanorina', '0370489612', 0),
       ('C2-A-CASH', 'col-2', 'CASH', NULL, NULL, 0),
       ('C2-A-MOBILE-1', 'col-2', 'ORANGE_MONEY', 'Dobo voalohany', '0320489612', 0),
       ('C3-A-CASH', 'col-3', 'CASH', NULL, NULL, 0),
       ('C3-A-BANK-1', 'col-3', 'BANK', 'Koto', NULL, 0),
       ('C3-A-BANK-2', 'col-3', 'BANK', 'Naivo', NULL, 0),
       ('C3-A-MOBILE-1', 'col-3', 'MVOLA', 'Kolo', '0341889612', 0);

UPDATE financial_account
SET bank_name      = 'BMOI',
    bank_code      = '00004',
    agency_code    = '00001',
    account_number = '1234567890',
    rib_key        = '12'
WHERE id = 'C3-A-BANK-1';
UPDATE financial_account
SET bank_name      = 'BRED',
    bank_code      = '00008',
    agency_code    = '00003',
    account_number = '4567890123',
    rib_key        = '58'
WHERE id = 'C3-A-BANK-2';

-- =====================================================
-- 17. INSERTION DES COTISATIONS
-- =====================================================
INSERT INTO membership_fee (id, label, status, frequency, eligible_from, amount)
VALUES ('cot-1', 'Cotisation annuelle', 'ACTIVE', 'ANNUALLY', '2026-01-01', 200000),
       ('cot-2', 'Famangiana', 'ACTIVE', 'PUNCTUALLY', '2026-04-30', 20000),
       ('cot-3', 'Cotisation annuelle', 'ACTIVE', 'ANNUALLY', '2026-01-01', 200000),
       ('cot-4', 'Cotisation 2025', 'INACTIVE', 'ANNUALLY', '2025-01-01', 100000),
       ('cot-5', 'Cotisation mensuelle', 'ACTIVE', 'MONTHLY', '2026-04-01', 25000);

-- =====================================================
-- 18. INSERTION DES LIENS COTISATION-COLLECTIVITÉ
-- =====================================================
INSERT INTO collectivity_membership_fee (id, collectivity_id, membership_fee_id)
VALUES ('cmf-1', 'col-1', 'cot-1'),
       ('cmf-2', 'col-1', 'cot-2'),
       ('cmf-3', 'col-2', 'cot-3'),
       ('cmf-4', 'col-2', 'cot-4'),
       ('cmf-5', 'col-3', 'cot-5');

-- =====================================================
-- INSERTION DES PAIEMENTS (UNIQUEMENT pour anciens membres)
-- =====================================================
INSERT INTO member_payment (id, member_id, membership_fee_id, amount, payment_mode, creation_date, account_credited_id)
VALUES
-- Collectivité 1
('pay-c1-1', 'C1-M1', 'cot-1', 200000, 'CASH', '2026-01-01', 'C1-A-CASH'),
('pay-c1-2', 'C1-M2', 'cot-1', 200000, 'CASH', '2026-01-01', 'C1-A-CASH'),
('pay-c1-3', 'C1-M3', 'cot-1', 200000, 'MOBILE_BANKING', '2026-01-01', 'C1-A-MOBILE-1'),
('pay-c1-4', 'C1-M4', 'cot-1', 200000, 'MOBILE_BANKING', '2026-01-01', 'C1-A-MOBILE-1'),
('pay-c1-5', 'C1-M5', 'cot-1', 150000, 'MOBILE_BANKING', '2026-01-01', 'C1-A-MOBILE-1'),
('pay-c1-6', 'C1-M6', 'cot-1', 100000, 'CASH', '2026-05-01', 'C1-A-CASH'),
('pay-c1-7', 'C1-M7', 'cot-1', 60000, 'CASH', '2026-05-01', 'C1-A-CASH'),
('pay-c1-8', 'C1-M8', 'cot-1', 90000, 'CASH', '2026-05-01', 'C1-A-CASH'),
-- Collectivité 2
('pay-c2-1', 'C2-M1', 'cot-3', 120000, 'CASH', '2026-01-01', 'C2-A-CASH'),
('pay-c2-2', 'C2-M2', 'cot-3', 180000, 'CASH', '2026-01-01', 'C2-A-CASH'),
('pay-c2-3', 'C2-M3', 'cot-3', 200000, 'CASH', '2026-01-01', 'C2-A-CASH'),
('pay-c2-4', 'C2-M4', 'cot-3', 200000, 'CASH', '2026-01-01', 'C2-A-CASH'),
('pay-c2-5', 'C2-M5', 'cot-3', 200000, 'CASH', '2026-01-01', 'C2-A-CASH'),
('pay-c2-6', 'C2-M6', 'cot-3', 200000, 'CASH', '2026-01-01', 'C2-A-CASH'),
('pay-c2-7', 'C2-M7', 'cot-3', 80000, 'MOBILE_BANKING', '2026-01-01', 'C2-A-MOBILE-1'),
('pay-c2-8', 'C2-M8', 'cot-3', 120000, 'MOBILE_BANKING', '2026-01-01', 'C2-A-MOBILE-1'),
-- Collectivité 3 (Avril 2026)
('pay-c3-1', 'C3-M1', 'cot-5', 25000, 'BANK_TRANSFER', '2026-04-01', 'C3-A-BANK-1'),
('pay-c3-2', 'C3-M2', 'cot-5', 25000, 'BANK_TRANSFER', '2026-04-01', 'C3-A-BANK-1'),
('pay-c3-3', 'C3-M3', 'cot-5', 25000, 'BANK_TRANSFER', '2026-04-01', 'C3-A-BANK-1'),
('pay-c3-4', 'C3-M4', 'cot-5', 25000, 'BANK_TRANSFER', '2026-04-01', 'C3-A-BANK-1'),
('pay-c3-5', 'C3-M5', 'cot-5', 25000, 'BANK_TRANSFER', '2026-04-01', 'C3-A-BANK-2'),
('pay-c3-6', 'C3-M6', 'cot-5', 25000, 'BANK_TRANSFER', '2026-04-01', 'C3-A-BANK-2'),
('pay-c3-7', 'C3-M7', 'cot-5', 25000, 'CASH', '2026-04-01', 'C3-A-CASH'),
('pay-c3-8', 'C3-M8', 'cot-5', 25000, 'CASH', '2026-04-01', 'C3-A-CASH'),
-- Collectivité 3 (Mai 2026)
('pay-c3-9', 'C3-M1', 'cot-5', 25000, 'BANK_TRANSFER', '2026-05-01', 'C3-A-BANK-1'),
('pay-c3-10', 'C3-M2', 'cot-5', 25000, 'BANK_TRANSFER', '2026-05-01', 'C3-A-BANK-1'),
('pay-c3-11', 'C3-M3', 'cot-5', 15000, 'MOBILE_BANKING', '2026-05-01', 'C3-A-MOBILE-1'),
('pay-c3-12', 'C3-M4', 'cot-5', 15000, 'MOBILE_BANKING', '2026-05-01', 'C3-A-MOBILE-1'),
('pay-c3-13', 'C3-M5', 'cot-5', 20000, 'BANK_TRANSFER', '2026-05-01', 'C3-A-BANK-2'),
('pay-c3-14', 'C3-M6', 'cot-5', 25000, 'BANK_TRANSFER', '2026-05-01', 'C3-A-BANK-2'),
('pay-c3-15', 'C3-M7', 'cot-5', 5000, 'CASH', '2026-05-01', 'C3-A-CASH'),
('pay-c3-16', 'C3-M8', 'cot-5', 5000, 'CASH', '2026-05-01', 'C3-A-CASH');


-- =====================================================
-- CRÉATION DES TABLES MANQUANTES POUR LES ACTIVITÉS
-- =====================================================

-- Table activity
DROP TABLE IF EXISTS activity CASCADE;
CREATE TABLE activity
(
    id                           VARCHAR PRIMARY KEY,
    label                        VARCHAR NOT NULL,
    activity_type                VARCHAR CHECK (activity_type IN ('MEETING', 'TRAINING', 'OTHER')),
    executive_date               DATE,
    recurrence_rule_week_ordinal INTEGER,
    recurrence_rule_day_of_week  VARCHAR CHECK (recurrence_rule_day_of_week IN
                                                ('MO', 'TU', 'WE', 'TH', 'FR', 'SA', 'SU'))
);

-- Table collectivity_activity
DROP TABLE IF EXISTS collectivity_activity CASCADE;
CREATE TABLE collectivity_activity
(
    id              VARCHAR PRIMARY KEY,
    collectivity_id VARCHAR REFERENCES "collectivity" (id) ON DELETE CASCADE,
    activity_id     VARCHAR REFERENCES activity (id) ON DELETE CASCADE
);

-- Table activity_member_attendance
DROP TABLE IF EXISTS activity_member_attendance CASCADE;
CREATE TABLE activity_member_attendance
(
    id                VARCHAR PRIMARY KEY,
    activity_id       VARCHAR REFERENCES activity (id) ON DELETE CASCADE,
    member_id         VARCHAR REFERENCES "member" (id) ON DELETE CASCADE,
    attendance_status VARCHAR CHECK (attendance_status IN ('ATTENDED', 'MISSING', 'UNDEFINED')),
    CONSTRAINT unique_activity_member UNIQUE (activity_id, member_id)
);

-- Table activity_member_occupation_concerned
DROP TABLE IF EXISTS activity_member_occupation_concerned CASCADE;
CREATE TABLE activity_member_occupation_concerned
(
    id          VARCHAR PRIMARY KEY,
    activity_id VARCHAR REFERENCES activity (id) ON DELETE CASCADE,
    occupation  member_occupation
);

-- =====================================================
-- CRÉATION DES TABLES POUR LES COTISATIONS
-- =====================================================

-- Table membership_fee_installment
DROP TABLE IF EXISTS membership_fee_installment CASCADE;
CREATE TABLE membership_fee_installment
(
    id                VARCHAR PRIMARY KEY,
    membership_fee_id VARCHAR REFERENCES membership_fee (id),
    member_id         VARCHAR REFERENCES "member" (id),
    due_date          DATE    NOT NULL,
    amount            DECIMAL NOT NULL,
    status            VARCHAR DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PAID', 'OVERDUE')),
    created_at        DATE    DEFAULT CURRENT_DATE,
    CONSTRAINT unique_member_fee_installment UNIQUE (membership_fee_id, member_id, due_date)
);

-- =====================================================
-- CRÉATION DES INDEX POUR OPTIMISER LES REQUÊTES
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_installment_member_due ON membership_fee_installment (member_id, due_date);
CREATE INDEX IF NOT EXISTS idx_installment_membership_fee ON membership_fee_installment (membership_fee_id);
CREATE INDEX IF NOT EXISTS idx_collectivity_activity_collectivity ON collectivity_activity (collectivity_id);
CREATE INDEX IF NOT EXISTS idx_collectivity_activity_activity ON collectivity_activity (activity_id);
CREATE INDEX IF NOT EXISTS idx_attendance_activity ON activity_member_attendance (activity_id);
CREATE INDEX IF NOT EXISTS idx_attendance_member ON activity_member_attendance (member_id);


-- Schema Bonus
-- =====================================================
-- INSERTION DES ACTIVITÉS (Bonus 1) - Version corrigée
-- Données de test - 6 mai 2026
-- =====================================================

-- =====================================================
-- 1. ACTIVITÉS COLLECTIVITÉ 1 (Tableau 21)
-- =====================================================

-- act-1: AG1 (MEETING) - 1er samedi de chaque mois
INSERT INTO activity (id, label, activity_type, recurrence_rule_week_ordinal, recurrence_rule_day_of_week)
VALUES ('act-1', 'AG1', 'MEETING', 1, 'SA')
ON CONFLICT (id) DO NOTHING;

-- act-2: Formation de base (TRAINING) - 2ème dimanche de chaque mois
INSERT INTO activity (id, label, activity_type, recurrence_rule_week_ordinal, recurrence_rule_day_of_week)
VALUES ('act-2', 'Formation de base', 'TRAINING', 2, 'SU')
ON CONFLICT (id) DO NOTHING;

-- Occupations concernées pour act-1 (tous les postes)
INSERT INTO activity_member_occupation_concerned (id, activity_id, occupation)
VALUES ('occ-1', 'act-1', 'JUNIOR'),
       ('occ-2', 'act-1', 'SENIOR'),
       ('occ-3', 'act-1', 'SECRETARY'),
       ('occ-4', 'act-1', 'TREASURER'),
       ('occ-5', 'act-1', 'VICE_PRESIDENT'),
       ('occ-6', 'act-1', 'PRESIDENT')
ON CONFLICT (id) DO NOTHING;

-- Occupations concernées pour act-2 (uniquement JUNIOR)
INSERT INTO activity_member_occupation_concerned (id, activity_id, occupation)
VALUES ('occ-7', 'act-2', 'JUNIOR')
ON CONFLICT (id) DO NOTHING;

-- Lier activités à la collectivité 1
INSERT INTO collectivity_activity (id, collectivity_id, activity_id)
VALUES ('ca-1', 'col-1', 'act-1'),
       ('ca-2', 'col-1', 'act-2')
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- 2. ACTIVITÉS COLLECTIVITÉ 2 (Tableau 22)
-- =====================================================

-- act-3: AG2 (MEETING) - 1er dimanche de chaque mois
INSERT INTO activity (id, label, activity_type, recurrence_rule_week_ordinal, recurrence_rule_day_of_week)
VALUES ('act-3', 'AG2', 'MEETING', 1, 'SU')
ON CONFLICT (id) DO NOTHING;

-- act-4: Formation de base (TRAINING) - 3ème dimanche de chaque mois
INSERT INTO activity (id, label, activity_type, recurrence_rule_week_ordinal, recurrence_rule_day_of_week)
VALUES ('act-4', 'Formation de base', 'TRAINING', 3, 'SU')
ON CONFLICT (id) DO NOTHING;

-- act-5: Perfectionnement (PUNCTUAL) - date fixe
INSERT INTO activity (id, label, activity_type, executive_date)
VALUES ('act-5', 'Perfectionnement', 'OTHER', '2026-04-30')
ON CONFLICT (id) DO NOTHING;

-- Occupations concernées pour act-3 (tous les postes)
INSERT INTO activity_member_occupation_concerned (id, activity_id, occupation)
VALUES ('occ-8', 'act-3', 'JUNIOR'),
       ('occ-9', 'act-3', 'SENIOR'),
       ('occ-10', 'act-3', 'SECRETARY'),
       ('occ-11', 'act-3', 'TREASURER'),
       ('occ-12', 'act-3', 'VICE_PRESIDENT'),
       ('occ-13', 'act-3', 'PRESIDENT')
ON CONFLICT (id) DO NOTHING;

-- Occupations concernées pour act-4 (uniquement JUNIOR)
INSERT INTO activity_member_occupation_concerned (id, activity_id, occupation)
VALUES ('occ-14', 'act-4', 'JUNIOR')
ON CONFLICT (id) DO NOTHING;

-- Occupations concernées pour act-5 (uniquement SENIOR)
INSERT INTO activity_member_occupation_concerned (id, activity_id, occupation)
VALUES ('occ-15', 'act-5', 'SENIOR')
ON CONFLICT (id) DO NOTHING;

-- Lier activités à la collectivité 2
INSERT INTO collectivity_activity (id, collectivity_id, activity_id)
VALUES ('ca-3', 'col-2', 'act-3'),
       ('ca-4', 'col-2', 'act-4'),
       ('ca-5', 'col-2', 'act-5')
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- 3. ACTIVITÉS COLLECTIVITÉ 3 (Tableau 23)
-- =====================================================

-- act-6: AG3 (MEETING) - 1er vendredi de chaque mois
INSERT INTO activity (id, label, activity_type, recurrence_rule_week_ordinal, recurrence_rule_day_of_week)
VALUES ('act-6', 'AG3', 'MEETING', 1, 'FR')
ON CONFLICT (id) DO NOTHING;

-- act-7: Formation de base (TRAINING) - 4ème mercredi de chaque mois
INSERT INTO activity (id, label, activity_type, recurrence_rule_week_ordinal, recurrence_rule_day_of_week)
VALUES ('act-7', 'Formation de base', 'TRAINING', 4, 'WE')
ON CONFLICT (id) DO NOTHING;

-- Occupations concernées pour act-6 (tous les postes)
INSERT INTO activity_member_occupation_concerned (id, activity_id, occupation)
VALUES ('occ-16', 'act-6', 'JUNIOR'),
       ('occ-17', 'act-6', 'SENIOR'),
       ('occ-18', 'act-6', 'SECRETARY'),
       ('occ-19', 'act-6', 'TREASURER'),
       ('occ-20', 'act-6', 'VICE_PRESIDENT'),
       ('occ-21', 'act-6', 'PRESIDENT')
ON CONFLICT (id) DO NOTHING;

-- Occupations concernées pour act-7 (uniquement JUNIOR)
INSERT INTO activity_member_occupation_concerned (id, activity_id, occupation)
VALUES ('occ-22', 'act-7', 'JUNIOR')
ON CONFLICT (id) DO NOTHING;

-- Lier activités à la collectivité 3
INSERT INTO collectivity_activity (id, collectivity_id, activity_id)
VALUES ('ca-6', 'col-3', 'act-6'),
       ('ca-7', 'col-3', 'act-7')
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- 4. PRÉSENCES - COLLECTIVITÉ 1 (Tableaux 24 et 25)
-- =====================================================

-- Supprimer les anciennes présences pour act-1 avant d'insérer
DELETE
FROM activity_member_attendance
WHERE activity_id = 'act-1';

-- act-1: AG1 du 07/03/2026 (Tableau 24)
INSERT INTO activity_member_attendance (id, activity_id, member_id, attendance_status)
VALUES ('att-1', 'act-1', 'C1-M1', 'ATTENDED'),
       ('att-2', 'act-1', 'C1-M2', 'ATTENDED'),
       ('att-3', 'act-1', 'C1-M3', 'ATTENDED'),
       ('att-4', 'act-1', 'C1-M4', 'ATTENDED'),
       ('att-5', 'act-1', 'C1-M5', 'ATTENDED'),
       ('att-6', 'act-1', 'C1-M6', 'ATTENDED'),
       ('att-7', 'act-1', 'C1-M7', 'MISSING'),
       ('att-8', 'act-1', 'C1-M8', 'MISSING')
ON CONFLICT (id) DO NOTHING;

-- act-1: AG1 du 04/04/2026 (Tableau 25) - Mise à jour des statuts
INSERT INTO activity_member_attendance (id, activity_id, member_id, attendance_status)
VALUES ('att-9', 'act-1', 'C1-M1', 'ATTENDED'),
       ('att-10', 'act-1', 'C1-M2', 'ATTENDED'),
       ('att-11', 'act-1', 'C1-M3', 'MISSING'),
       ('att-12', 'act-1', 'C1-M4', 'MISSING'),
       ('att-13', 'act-1', 'C1-M5', 'ATTENDED'),
       ('att-14', 'act-1', 'C1-M6', 'ATTENDED'),
       ('att-15', 'act-1', 'C1-M7', 'ATTENDED'),
       ('att-16', 'act-1', 'C1-M8', 'ATTENDED')
ON CONFLICT (activity_id, member_id) DO UPDATE SET attendance_status = EXCLUDED.attendance_status;

-- =====================================================
-- 5. PRÉSENCES - COLLECTIVITÉ 2 (Tableaux 26, 27, 28)
-- =====================================================

-- Supprimer les anciennes présences pour act-3 avant d'insérer
DELETE
FROM activity_member_attendance
WHERE activity_id = 'act-3';

-- act-3: AG2 du 08/03/2026 (Tableau 26)
INSERT INTO activity_member_attendance (id, activity_id, member_id, attendance_status)
VALUES ('att-17', 'act-3', 'C2-M1', 'ATTENDED'),
       ('att-18', 'act-3', 'C2-M2', 'ATTENDED'),
       ('att-19', 'act-3', 'C2-M3', 'MISSING'),
       ('att-20', 'act-3', 'C2-M4', 'MISSING'),
       ('att-21', 'act-3', 'C2-M5', 'ATTENDED'),
       ('att-22', 'act-3', 'C2-M6', 'ATTENDED'),
       ('att-23', 'act-3', 'C2-M7', 'ATTENDED'),
       ('att-24', 'act-3', 'C2-M8', 'ATTENDED')
ON CONFLICT (id) DO NOTHING;

-- act-3: AG2 du 05/04/2026 (Tableau 27)
INSERT INTO activity_member_attendance (id, activity_id, member_id, attendance_status)
VALUES ('att-25', 'act-3', 'C2-M1', 'ATTENDED'),
       ('att-26', 'act-3', 'C2-M2', 'ATTENDED'),
       ('att-27', 'act-3', 'C2-M3', 'MISSING'),
       ('att-28', 'act-3', 'C2-M4', 'ATTENDED'),
       ('att-29', 'act-3', 'C2-M5', 'ATTENDED'),
       ('att-30', 'act-3', 'C2-M6', 'ATTENDED'),
       ('att-31', 'act-3', 'C2-M7', 'ATTENDED'),
       ('att-32', 'act-3', 'C2-M8', 'MISSING')
ON CONFLICT (activity_id, member_id) DO UPDATE SET attendance_status = EXCLUDED.attendance_status;

-- act-5: Perfectionnement du 30/04/2026 (Tableau 28)
INSERT INTO activity_member_attendance (id, activity_id, member_id, attendance_status)
VALUES ('att-33', 'act-5', 'C2-M1', 'ATTENDED'),
       ('att-34', 'act-5', 'C2-M2', 'ATTENDED'),
       ('att-35', 'act-5', 'C2-M3', 'ATTENDED'),
       ('att-36', 'act-5', 'C2-M4', 'MISSING'),
       ('att-37', 'act-5', 'C2-M5', 'UNDEFINED'),
       ('att-38', 'act-5', 'C2-M6', 'UNDEFINED'),
       ('att-39', 'act-5', 'C2-M7', 'UNDEFINED'),
       ('att-40', 'act-5', 'C2-M8', 'UNDEFINED')
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- 6. PRÉSENCES - COLLECTIVITÉ 3 (Tableaux 29 et 30)
-- =====================================================

-- Supprimer les anciennes présences pour act-6 avant d'insérer
DELETE
FROM activity_member_attendance
WHERE activity_id = 'act-6';

-- act-6: AG3 du 06/03/2026 (Tableau 29)
INSERT INTO activity_member_attendance (id, activity_id, member_id, attendance_status)
VALUES ('att-41', 'act-6', 'C3-M1', 'ATTENDED'),
       ('att-42', 'act-6', 'C3-M2', 'ATTENDED'),
       ('att-43', 'act-6', 'C3-M3', 'ATTENDED'),
       ('att-44', 'act-6', 'C3-M4', 'ATTENDED'),
       ('att-45', 'act-6', 'C3-M5', 'ATTENDED'),
       ('att-46', 'act-6', 'C3-M6', 'ATTENDED'),
       ('att-47', 'act-6', 'C3-M7', 'MISSING'),
       ('att-48', 'act-6', 'C3-M8', 'MISSING')
ON CONFLICT (id) DO NOTHING;

-- act-6: AG3 du 03/04/2026 (Tableau 30)
INSERT INTO activity_member_attendance (id, activity_id, member_id, attendance_status)
VALUES ('att-49', 'act-6', 'C3-M1', 'ATTENDED'),
       ('att-50', 'act-6', 'C3-M2', 'ATTENDED'),
       ('att-51', 'act-6', 'C3-M3', 'MISSING'),
       ('att-52', 'act-6', 'C3-M4', 'MISSING'),
       ('att-53', 'act-6', 'C3-M5', 'ATTENDED'),
       ('att-54', 'act-6', 'C3-M6', 'ATTENDED'),
       ('att-55', 'act-6', 'C3-M7', 'MISSING'),
       ('att-56', 'act-6', 'C3-M8', 'ATTENDED'),
       ('att-57', 'act-6', 'C1-M1', 'ATTENDED')
ON CONFLICT (activity_id, member_id) DO UPDATE SET attendance_status = EXCLUDED.attendance_status;

-- =====================================================
-- 7. VÉRIFICATION FINALE
-- =====================================================
DO
$$
    DECLARE
        activity_count   INTEGER;
        attendance_count INTEGER;
    BEGIN
        SELECT COUNT(*) INTO activity_count FROM activity;
        SELECT COUNT(*) INTO attendance_count FROM activity_member_attendance;

        RAISE NOTICE '=====================================================';
        RAISE NOTICE 'DONNEES BONUS 1 INSERTEES AVEC SUCCES !';
        RAISE NOTICE '=====================================================';
        RAISE NOTICE 'Nombre d activites : %', activity_count;
        RAISE NOTICE 'Nombre de presences : %', attendance_count;
        RAISE NOTICE '=====================================================';
    END
$$;