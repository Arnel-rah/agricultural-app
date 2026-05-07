-- =====================================================
-- SCRIPT COMPLET POUR L'AGRICULTURAL FEDERATION API
-- Création des tables et insertion des données de test
-- =====================================================

-- Supprimer les tables existantes (ordre inverse des dépendances)
DROP TABLE IF EXISTS activity_member_attendance CASCADE;
DROP TABLE IF EXISTS collectivity_activity CASCADE;
DROP TABLE IF EXISTS activity_member_occupation_concerned CASCADE;
DROP TABLE IF EXISTS member_payment CASCADE;
DROP TABLE IF EXISTS collectivity_membership_fee CASCADE;
DROP TABLE IF EXISTS membership_fee CASCADE;
DROP TABLE IF EXISTS member_referee CASCADE;
DROP TABLE IF EXISTS collectivity_member CASCADE;
DROP TABLE IF EXISTS activity CASCADE;
DROP TABLE IF EXISTS "member" CASCADE;
DROP TABLE IF EXISTS "collectivity" CASCADE;

-- =====================================================
-- 1. CRÉATION DES TYPES ENUM
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
-- 2. TABLE MEMBER
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
-- 3. TABLE COLLECTIVITY
-- =====================================================
CREATE TABLE "collectivity"
(
    id                VARCHAR PRIMARY KEY,
    name              VARCHAR,
    number            INTEGER,
    location          VARCHAR,
    specialization    VARCHAR,
    president_id      VARCHAR REFERENCES "member" (id),
    vice_president_id VARCHAR REFERENCES "member" (id),
    treasurer_id      VARCHAR REFERENCES "member" (id),
    secretary_id      VARCHAR REFERENCES "member" (id)
);

-- =====================================================
-- 4. TABLE COLLECTIVITY_MEMBER
-- =====================================================
CREATE TABLE "collectivity_member"
(
    id              VARCHAR PRIMARY KEY,
    member_id       VARCHAR REFERENCES "member" (id),
    collectivity_id VARCHAR REFERENCES "collectivity" (id)
);

-- =====================================================
-- 5. TABLE MEMBER_REFEREE
-- =====================================================
CREATE TABLE "member_referee"
(
    id                 VARCHAR PRIMARY KEY,
    member_refereed_id VARCHAR REFERENCES "member" (id),
    member_referee_id  VARCHAR REFERENCES "member" (id)
);

-- =====================================================
-- 6. TABLE MEMBERSHIP_FEE
-- =====================================================
CREATE TABLE "membership_fee"
(
    id            VARCHAR PRIMARY KEY,
    eligible_from DATE,
    frequency     VARCHAR,
    amount        DECIMAL,
    label         VARCHAR,
    status        VARCHAR CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

-- =====================================================
-- 7. TABLE COLLECTIVITY_MEMBERSHIP_FEE
-- =====================================================
CREATE TABLE "collectivity_membership_fee"
(
    id                VARCHAR PRIMARY KEY,
    collectivity_id   VARCHAR REFERENCES "collectivity" (id),
    membership_fee_id VARCHAR REFERENCES "membership_fee" (id)
);

-- =====================================================
-- 8. TABLE MEMBER_PAYMENT
-- =====================================================
CREATE TABLE "member_payment"
(
    id                  VARCHAR PRIMARY KEY,
    member_id           VARCHAR REFERENCES "member" (id),
    membership_fee_id   VARCHAR REFERENCES "membership_fee" (id),
    amount              DECIMAL,
    payment_mode        VARCHAR CHECK (payment_mode IN ('CASH', 'MOBILE_BANKING', 'BANK_TRANSFER')),
    creation_date       DATE DEFAULT CURRENT_DATE,
    account_credited_id VARCHAR
);

-- =====================================================
-- 9. TABLE ACTIVITY
-- =====================================================
CREATE TABLE "activity"
(
    id                           VARCHAR PRIMARY KEY,
    label                        VARCHAR NOT NULL,
    activity_type                VARCHAR CHECK (activity_type IN ('MEETING', 'TRAINING', 'OTHER')),
    executive_date               DATE,
    recurrence_rule_week_ordinal INTEGER,
    recurrence_rule_day_of_week  VARCHAR CHECK (recurrence_rule_day_of_week IN
                                                ('MO', 'TU', 'WE', 'TH', 'FR', 'SA', 'SU'))
);

-- =====================================================
-- 10. TABLE COLLECTIVITY_ACTIVITY
-- =====================================================
CREATE TABLE "collectivity_activity"
(
    id              VARCHAR PRIMARY KEY,
    collectivity_id VARCHAR REFERENCES "collectivity" (id) ON DELETE CASCADE,
    activity_id     VARCHAR REFERENCES "activity" (id) ON DELETE CASCADE
);

-- =====================================================
-- 11. TABLE ACTIVITY_MEMBER_ATTENDANCE
-- =====================================================
CREATE TABLE "activity_member_attendance"
(
    id                VARCHAR PRIMARY KEY,
    activity_id       VARCHAR REFERENCES "activity" (id) ON DELETE CASCADE,
    member_id         VARCHAR REFERENCES "member" (id) ON DELETE CASCADE,
    attendance_status VARCHAR CHECK (attendance_status IN ('ATTENDED', 'MISSING', 'UNDEFINED')),
    CONSTRAINT unique_activity_member UNIQUE (activity_id, member_id)
);

-- =====================================================
-- 12. TABLE ACTIVITY_MEMBER_OCCUPATION_CONCERNED
-- =====================================================
CREATE TABLE "activity_member_occupation_concerned"
(
    id          VARCHAR PRIMARY KEY,
    activity_id VARCHAR REFERENCES "activity" (id) ON DELETE CASCADE,
    occupation  member_occupation
);

-- =====================================================
-- =====================================================
-- INSERTION DES DONNÉES DE TEST
-- =====================================================
-- =====================================================

-- =====================================================
-- 1. INSERTION DES MEMBRES
-- =====================================================
INSERT INTO "member" (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email,
                      occupation, registration_fee_paid, membership_dues_paid, created_at)
VALUES ('C1-M1', 'Prénom membre 1', 'Nom membre 1', '1980-02-01', 'MALE', 'Lot II V M Ambato.', 'Riziculture',
        '0341234567', 'member.1@fed-agri.mg', 'PRESIDENT', true, true, '2024-01-15'),
       ('C1-M2', 'Prénom membre 2', 'Nom membre 2', '1982-03-05', 'MALE', 'Lot II F Ambato.', 'Agriculteur',
        '0321234567', 'member.2@fed-agri.mg', 'VICE_PRESIDENT', true, true, '2024-01-20'),
       ('C1-M3', 'Prénom membre 3', 'Nom membre 3', '1985-06-10', 'FEMALE', 'Lot III A Ambato.', 'Secrétaire',
        '0331234567', 'member.3@fed-agri.mg', 'SECRETARY', true, true, '2024-02-10'),
       ('C1-M4', 'Prénom membre 4', 'Nom membre 4', '1983-08-15', 'MALE', 'Lot IV B Ambato.', 'Trésorier', '0341234568',
        'member.4@fed-agri.mg', 'TREASURER', true, true, '2024-02-15'),
       ('C1-M5', 'Prénom membre 5', 'Nom membre 5', '1990-01-20', 'MALE', 'Lot V C Ambato.', 'Agriculteur',
        '0321234568', 'member.5@fed-agri.mg', 'SENIOR', true, true, '2024-03-01'),
       ('C1-M6', 'Prénom membre 6', 'Nom membre 6', '1992-04-25', 'FEMALE', 'Lot VI D Ambato.', 'Éleveuse',
        '0331234568', 'member.6@fed-agri.mg', 'SENIOR', true, true, '2024-03-10'),
       ('C1-M7', 'Prénom membre 7', 'Nom membre 7', '1988-07-30', 'MALE', 'Lot VII E Ambato.', 'Agriculteur',
        '0341234569', 'member.7@fed-agri.mg', 'SENIOR', true, true, '2024-11-01'),
       ('C1-M8', 'Prénom membre 8', 'Nom membre 8', '1995-10-05', 'FEMALE', 'Lot VIII F Ambato.', 'Commerçante',
        '0321234569', 'member.8@fed-agri.mg', 'SENIOR', true, true, '2024-12-01'),
       ('C1-M9', 'Prénom membre 9', 'Nom membre 9', '1991-12-12', 'MALE', 'Lot IX G Ambato.', 'Agriculteur',
        '0331234569', 'member.9@fed-agri.mg', 'JUNIOR', true, true, '2024-12-15'),
       ('C1-M10', 'Prénom membre 10', 'Nom membre 10', '1993-02-18', 'FEMALE', 'Lot X H Ambato.', 'Étudiante',
        '0341234570', 'member.10@fed-agri.mg', 'JUNIOR', true, true, '2024-12-20');

-- =====================================================
-- 2. INSERTION DES COLLECTIVITÉS
-- =====================================================
INSERT INTO "collectivity" (id, name, number, location, president_id, vice_president_id, treasurer_id, secretary_id)
VALUES ('col-1', 'Mpanorina', 1, 'Analamanga', 'C1-M1', 'C1-M2', 'C1-M4', 'C1-M3'),
       ('col-2', 'Dobo voalohany', 2, 'Vakinankaratra', NULL, NULL, NULL, NULL),
       ('col-3', 'Tantely mamy', 3, 'Haute Matsiatra', NULL, NULL, NULL, NULL);

-- =====================================================
-- 3. INSERTION DES LIENS COLLECTIVITÉ-MEMBRE
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
       ('cm-9', 'C1-M9', 'col-1'),
       ('cm-10', 'C1-M10', 'col-1');

-- =====================================================
-- 4. INSERTION DES RÉFÉRANTS
-- =====================================================
INSERT INTO "member_referee" (id, member_refereed_id, member_referee_id)
VALUES ('ref-1', 'C1-M3', 'C1-M1'),
       ('ref-2', 'C1-M3', 'C1-M2'),
       ('ref-3', 'C1-M4', 'C1-M1'),
       ('ref-4', 'C1-M4', 'C1-M2'),
       ('ref-5', 'C1-M5', 'C1-M1');

-- =====================================================
-- 5. INSERTION DES COTISATIONS
-- =====================================================
INSERT INTO "membership_fee" (id, eligible_from, frequency, amount, label, status)
VALUES ('fee-1', '2024-01-01', 'MONTHLY', 5000, 'Cotisation mensuelle 2024', 'ACTIVE'),
       ('fee-2', '2025-01-01', 'MONTHLY', 5500, 'Cotisation mensuelle 2025', 'ACTIVE');

-- =====================================================
-- 6. INSERTION DES LIENS COTISATION-COLLECTIVITÉ
-- =====================================================
INSERT INTO "collectivity_membership_fee" (id, collectivity_id, membership_fee_id)
VALUES ('cmf-1', 'col-1', 'fee-1'),
       ('cmf-2', 'col-2', 'fee-1'),
       ('cmf-3', 'col-3', 'fee-1');

-- =====================================================
-- 7. INSERTION DES PAIEMENTS
-- =====================================================
INSERT INTO "member_payment" (id, member_id, membership_fee_id, amount, payment_mode, creation_date)
VALUES ('pay-1', 'C1-M1', 'fee-1', 5000, 'CASH', '2024-01-15'),
       ('pay-2', 'C1-M1', 'fee-1', 5000, 'CASH', '2024-02-15'),
       ('pay-3', 'C1-M1', 'fee-1', 5000, 'CASH', '2024-03-15'),
       ('pay-4', 'C1-M2', 'fee-1', 5000, 'CASH', '2024-01-20'),
       ('pay-5', 'C1-M2', 'fee-1', 5000, 'CASH', '2024-02-20'),
       ('pay-6', 'C1-M3', 'fee-1', 5000, 'CASH', '2024-02-10'),
       ('pay-7', 'C1-M4', 'fee-1', 5000, 'CASH', '2024-02-15');

-- =====================================================
-- 8. INSERTION DES ACTIVITÉS
-- =====================================================
INSERT INTO "activity" (id, label, activity_type, executive_date, recurrence_rule_week_ordinal,
                        recurrence_rule_day_of_week)
VALUES ('act-1', 'Réunion mensuelle Janvier', 'MEETING', '2024-01-20', NULL, NULL),
       ('act-2', 'Formation agricole', 'TRAINING', '2024-02-15', NULL, NULL),
       ('act-3', 'Assemblée générale', 'MEETING', '2024-03-10', NULL, NULL),
       ('act-4', 'Atelier technique', 'TRAINING', '2024-07-10', NULL, NULL),
       ('act-5', 'Réunion bilan', 'MEETING', '2024-08-15', NULL, NULL),
       ('act-6', 'Formation leadership', 'TRAINING', '2024-09-20', NULL, NULL),
       ('act-7', 'Activité récurrente', 'MEETING', NULL, 3, 'SA');

-- =====================================================
-- 9. INSERTION DES LIENS ACTIVITÉ-COLLECTIVITÉ
-- =====================================================
INSERT INTO "collectivity_activity" (id, collectivity_id, activity_id)
VALUES ('ca-1', 'col-1', 'act-1'),
       ('ca-2', 'col-1', 'act-2'),
       ('ca-3', 'col-1', 'act-3'),
       ('ca-4', 'col-1', 'act-4'),
       ('ca-5', 'col-1', 'act-5'),
       ('ca-6', 'col-1', 'act-6'),
       ('ca-7', 'col-1', 'act-7'),
       ('ca-8', 'col-2', 'act-1'),
       ('ca-9', 'col-3', 'act-1');

-- =====================================================
-- 10. INSERTION DES PRÉSENCES AUX ACTIVITÉS
-- =====================================================
INSERT INTO "activity_member_attendance" (id, activity_id, member_id, attendance_status)
VALUES ('att-1', 'act-1', 'C1-M1', 'ATTENDED'),
       ('att-2', 'act-1', 'C1-M2', 'ATTENDED'),
       ('att-3', 'act-1', 'C1-M3', 'MISSING'),
       ('att-4', 'act-1', 'C1-M4', 'ATTENDED'),
       ('att-5', 'act-1', 'C1-M5', 'ATTENDED'),
       ('att-6', 'act-1', 'C1-M6', 'MISSING'),
       ('att-7', 'act-1', 'C1-M7', 'ATTENDED'),
       ('att-8', 'act-1', 'C1-M8', 'ATTENDED'),
       ('att-9', 'act-2', 'C1-M1', 'ATTENDED'),
       ('att-10', 'act-2', 'C1-M2', 'MISSING'),
       ('att-11', 'act-2', 'C1-M3', 'ATTENDED'),
       ('att-12', 'act-2', 'C1-M4', 'ATTENDED'),
       ('att-13', 'act-2', 'C1-M5', 'MISSING'),
       ('att-14', 'act-3', 'C1-M1', 'ATTENDED'),
       ('att-15', 'act-3', 'C1-M2', 'ATTENDED'),
       ('att-16', 'act-3', 'C1-M3', 'ATTENDED'),
       ('att-17', 'act-3', 'C1-M4', 'ATTENDED'),
       ('att-18', 'act-4', 'C1-M1', 'ATTENDED'),
       ('att-19', 'act-4', 'C1-M2', 'ATTENDED'),
       ('att-20', 'act-5', 'C1-M1', 'MISSING'),
       ('att-21', 'act-5', 'C1-M2', 'ATTENDED'),
       ('att-22', 'act-6', 'C1-M1', 'ATTENDED');

-- =====================================================
-- 11. INSERTION DES OCCUPATIONS CONCERNÉES PAR ACTIVITÉ
-- =====================================================
INSERT INTO "activity_member_occupation_concerned" (id, activity_id, occupation)
VALUES ('occ-1', 'act-1', 'PRESIDENT'),
       ('occ-2', 'act-1', 'SECRETARY'),
       ('occ-3', 'act-2', 'SENIOR'),
       ('occ-4', 'act-3', 'PRESIDENT'),
       ('occ-5', 'act-3', 'VICE_PRESIDENT'),
       ('occ-6', 'act-3', 'SECRETARY'),
       ('occ-7', 'act-3', 'TREASURER');

-- =====================================================
-- 12. VÉRIFICATION FINALE
-- =====================================================
DO
$$
    DECLARE
        member_count       INTEGER;
        collectivity_count INTEGER;
        activity_count     INTEGER;
        attendance_count   INTEGER;
    BEGIN
        SELECT COUNT(*) INTO member_count FROM "member";
        SELECT COUNT(*) INTO collectivity_count FROM "collectivity";
        SELECT COUNT(*) INTO activity_count FROM "activity";
        SELECT COUNT(*) INTO attendance_count FROM "activity_member_attendance";

        RAISE NOTICE '==========================================';
        RAISE NOTICE 'BASE DE DONNÉES INITIALISÉE AVEC SUCCÈS !';
        RAISE NOTICE '==========================================';
        RAISE NOTICE 'Nombre de membres : %', member_count;
        RAISE NOTICE 'Nombre de collectivités : %', collectivity_count;
        RAISE NOTICE 'Nombre d activités : %', activity_count;
        RAISE NOTICE 'Nombre de présences : %', attendance_count;
        RAISE NOTICE '==========================================';
    END
$$;

-- =====================================================
-- 13. AFFICHAGE DES RÉSULTATS POUR VÉRIFICATION
-- =====================================================
SELECT '=== MEMBRES ===' as "";
SELECT id, first_name, last_name, occupation, created_at
FROM "member";

SELECT '=== COLLECTIVITÉS ===' as "";
SELECT id, name, number, location
FROM "collectivity";

SELECT '=== ACTIVITÉS ===' as "";
SELECT id, label, activity_type, executive_date
FROM "activity";

SELECT '=== STATISTIQUES DES PRÉSENCES ===' as "";
SELECT a.label,
       COUNT(ama.id)                                                  as total_presences,
       COUNT(CASE WHEN ama.attendance_status = 'ATTENDED' THEN 1 END) as attended,
       COUNT(CASE WHEN ama.attendance_status = 'MISSING' THEN 1 END)  as missing
FROM activity a
         LEFT JOIN activity_member_attendance ama ON a.id = ama.activity_id
GROUP BY a.id, a.label
ORDER BY a.executive_date;


-- Ajouter la colonne due_date
ALTER TABLE membership_fee
    ADD COLUMN IF NOT EXISTS due_date DATE;

-- Mettre à jour avec eligible_from par défaut
UPDATE membership_fee
SET due_date = eligible_from
WHERE due_date IS NULL;


-- Table des échéances de cotisations
CREATE TABLE IF NOT EXISTS membership_fee_installment
(
    id                VARCHAR PRIMARY KEY,
    membership_fee_id VARCHAR REFERENCES membership_fee (id),
    member_id         VARCHAR REFERENCES member (id),
    due_date          DATE    NOT NULL,
    amount            DECIMAL NOT NULL,
    status            VARCHAR DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PAID', 'OVERDUE')),
    created_at        DATE    DEFAULT CURRENT_DATE
);

-- Index pour optimiser les recherches
CREATE INDEX IF NOT EXISTS idx_installment_member_due ON membership_fee_installment (member_id, due_date);
CREATE INDEX IF NOT EXISTS idx_installment_membership_fee ON membership_fee_installment (membership_fee_id);