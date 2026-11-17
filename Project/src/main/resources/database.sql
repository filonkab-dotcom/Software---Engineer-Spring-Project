//* ----------------------------------------------------------- */
/* 1) USERS (με discriminator για Student/Teacher/… κ.λπ.)      */
/* ----------------------------------------------------------- */
USE database_db;
CREATE TABLE users (
                       id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
                       dtype               VARCHAR(31) NOT NULL,          -- "USER", "STUDENT", …
                       first_name          VARCHAR(100),
                       last_name           VARCHAR(100),
                       email               VARCHAR(190) NOT NULL UNIQUE,
                       password            VARCHAR(255) NOT NULL,

    /* security‑meta (login‑lock) */
                       failed_attempts     INT  NOT NULL DEFAULT 0,
                       account_non_locked  BOOLEAN NOT NULL DEFAULT TRUE,
                       lock_time           DATETIME,

    /* πεδία STUDENT (SINGLE_TABLE strategy) */
                       university_id       VARCHAR(30),
                       preferred_location  VARCHAR(120)
) ENGINE=InnoDB;

/* 2) STUDENT <collection> → INTERESTS & SKILLS                 */
CREATE TABLE student_interests (
                                   user_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                   interest  VARCHAR(120) NOT NULL,
                                   PRIMARY KEY (user_id, interest)
) ENGINE=InnoDB;

CREATE TABLE student_skills (
                                user_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                skill    VARCHAR(120) NOT NULL,
                                PRIMARY KEY (user_id, skill)
) ENGINE=InnoDB;

/* 3) TRAINEESHIP APPLICATION                                   */
CREATE TABLE traineeship_applications (
                                          id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          student_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                          traineeship_id  BIGINT NOT NULL,              -- FK σε πίνακα Traineeship (όταν δημιουργηθεί)
                                          status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                          submitted_at    DATETIME  NOT NULL DEFAULT NOW()
) ENGINE=InnoDB;

/* 4) LOGBOOK ENTRIES                                           */
CREATE TABLE logbook_entries (
                                 id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 application_id  BIGINT NOT NULL REFERENCES traineeship_applications(id) ON DELETE CASCADE,
                                 entry_date      DATE NOT NULL,
                                 description     TEXT
) ENGINE=InnoDB;

/* ✔️ χρήσιμα indexes για performance */
CREATE INDEX idx_app_student_status  ON traineeship_applications (student_id, status);
CREATE INDEX idx_log_app_date        ON logbook_entries (application_id, entry_date);

/* 1)  interests των καθηγητών (professor_interests)              */
CREATE TABLE professor_interests (
                                     user_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                     interest  VARCHAR(120) NOT NULL,
                                     PRIMARY KEY (user_id, interest)
) ENGINE=InnoDB;

/* 2)  θέσεις πρακτικής που επιβλέπει καθηγητής                    */
CREATE TABLE traineeship_positions (
                                       id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       title         VARCHAR(255) NOT NULL,
                                       professor_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE
    -- + ό,τι άλλα πεδία θελήσεις (company_id, description κ.λπ.)
) ENGINE=InnoDB;
CREATE INDEX idx_position_professor ON traineeship_positions (professor_id);

/* 3)  αξιολογήσεις πρακτικών (από καθηγητές)                      */
CREATE TABLE traineeship_evaluations (
                                         id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         professor_id             BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                         traineeship_position_id  BIGINT NOT NULL REFERENCES traineeship_positions(id) ON DELETE CASCADE,
    /* φοιτητής */
                                         student_motivation       INT NOT NULL CHECK (student_motivation BETWEEN 1 AND 5),
                                         student_effectiveness    INT NOT NULL CHECK (student_effectiveness BETWEEN 1 AND 5),
                                         student_efficiency       INT NOT NULL CHECK (student_efficiency BETWEEN 1 AND 5),
    /* εταιρεία */
                                         company_facilities       INT NOT NULL CHECK (company_facilities BETWEEN 1 AND 5),
                                         company_guidance         INT NOT NULL CHECK (company_guidance BETWEEN 1 AND 5),
                                         created_at               DATETIME NOT NULL DEFAULT NOW()
) ENGINE=InnoDB;
CREATE INDEX idx_eval_prof     ON traineeship_evaluations (professor_id);
CREATE INDEX idx_eval_position ON traineeship_evaluations (traineeship_position_id);

/* 1) Αγγελίες πρακτικής της εταιρείας */
/* ---------- company_positions ---------- */
CREATE TABLE company_positions (
                                   id                BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   company_id        BIGINT  NOT NULL,
                                   start_date        DATE    NOT NULL,
                                   end_date          DATE    NOT NULL,
                                   description       TEXT,
                                   status            ENUM('AVAILABLE','ASSIGNED','DELETED') DEFAULT 'AVAILABLE',
                                   assigned_student_id BIGINT,
                                   created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   CONSTRAINT fk_company_pos_company
                                       FOREIGN KEY (company_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_company_pos_company  ON company_positions(company_id);
CREATE INDEX idx_company_pos_status   ON company_positions(company_id,status);

/* ---------- company_position_skills ---------- */
CREATE TABLE company_position_skills (
                                         position_id BIGINT NOT NULL,
                                         skill       VARCHAR(120) NOT NULL,
                                         PRIMARY KEY (position_id, skill),
                                         CONSTRAINT fk_skill_position
                                             FOREIGN KEY (position_id) REFERENCES company_positions(id) ON DELETE CASCADE
) ENGINE=InnoDB;

/* ---------- company_position_topics ---------- */
CREATE TABLE company_position_topics (
                                         position_id BIGINT NOT NULL,
                                         topic       VARCHAR(120) NOT NULL,
                                         PRIMARY KEY (position_id, topic),
                                         CONSTRAINT fk_topic_position
                                             FOREIGN KEY (position_id) REFERENCES company_positions(id) ON DELETE CASCADE
) ENGINE=InnoDB;

/* ---------- company_evaluations ---------- */
CREATE TABLE company_evaluations (
                                     id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     company_id           BIGINT NOT NULL,
                                     position_id          BIGINT NOT NULL,
                                     student_motivation   TINYINT NOT NULL,
                                     student_effectiveness TINYINT NOT NULL,
                                     student_efficiency    TINYINT NOT NULL,
                                     created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     CONSTRAINT fk_eval_company
                                         FOREIGN KEY (company_id)  REFERENCES users(id) ON DELETE CASCADE,
                                     CONSTRAINT fk_eval_position
                                         FOREIGN KEY (position_id) REFERENCES company_positions(id) ON DELETE CASCADE,
                                     CONSTRAINT chk_eval_motivation   CHECK (student_motivation   BETWEEN 1 AND 5),
                                     CONSTRAINT chk_eval_effectiveness CHECK (student_effectiveness BETWEEN 1 AND 5),
                                     CONSTRAINT chk_eval_efficiency   CHECK (student_efficiency   BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE INDEX idx_comp_eval_company  ON company_evaluations(company_id);
CREATE INDEX idx_comp_eval_position ON company_evaluations(position_id);
