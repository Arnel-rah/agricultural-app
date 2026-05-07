-- COLLECTIVITIES
CREATE TABLE collectivities (
                                id VARCHAR(10) PRIMARY KEY,
                                number INT UNIQUE,
                                name VARCHAR(100) UNIQUE,
                                city VARCHAR(100),
                                specialization VARCHAR(100)
);

-- MEMBERS
CREATE TABLE members (
                         id VARCHAR(10) PRIMARY KEY,
                         collectivities_id VARCHAR(10),
                         first_name VARCHAR(100),
                         last_name VARCHAR(100),
                         birth_date DATE,
                         gender CHAR(1),
                         address TEXT,
                         profession VARCHAR(100),
                         phone VARCHAR(20),
                         email VARCHAR(150),
                         role VARCHAR(50),
                         FOREIGN KEY (collectivities_id) REFERENCES collectivities(id)
);

-- SPONSORSHIP (relation membres référents)
CREATE TABLE member_sponsors (
                                 member_id VARCHAR(10),
                                 sponsor_id VARCHAR(10),
                                 PRIMARY KEY (member_id, sponsor_id),
                                 FOREIGN KEY (member_id) REFERENCES members(id),
                                 FOREIGN KEY (sponsor_id) REFERENCES members(id)
);

-- MEMBERSHIP FEES
CREATE TABLE membership_fees (
                                 id VARCHAR(10) PRIMARY KEY,
                                 collectivities_id VARCHAR(10),
                                 label VARCHAR(100),
                                 status VARCHAR(20),
                                 frequency VARCHAR(20),
                                 eligible_from DATE,
                                 amount INT,
                                 FOREIGN KEY (collectivities_id) REFERENCES collectivities(id)
);

-- ACCOUNTS
CREATE TABLE accounts (
                          id VARCHAR(20) PRIMARY KEY,
                          collectivities_id VARCHAR(10),
                          type VARCHAR(50),
                          balance INT,
                          owner_name VARCHAR(100),
                          phone VARCHAR(20),
                          FOREIGN KEY (collectivities_id) REFERENCES collectivities(id)
);

-- PAYMENTS
CREATE TABLE payments (
                          id SERIAL PRIMARY KEY,
                          collectivities_id VARCHAR(10),
                          member_id VARCHAR(10),
                          amount INT,
                          account_id VARCHAR(20),
                          payment_method VARCHAR(50),
                          payment_date DATE,
                          FOREIGN KEY (collectivities_id) REFERENCES collectivities(id),
                          FOREIGN KEY (member_id) REFERENCES members(id),
                          FOREIGN KEY (account_id) REFERENCES accounts(id)
);

-- TRANSACTIONS
CREATE TABLE transactions (
                              id SERIAL PRIMARY KEY,
                              collectivities_id VARCHAR(10),
                              member_id VARCHAR(10),
                              amount INT,
                              account_id VARCHAR(20),
                              payment_method VARCHAR(50),
                              created_at DATE,
                              FOREIGN KEY (collectivities_id) REFERENCES collectivities(id),
                              FOREIGN KEY (member_id) REFERENCES members(id),
                              FOREIGN KEY (account_id) REFERENCES accounts(id)
);