-- Schema for when the database is ready (MySQL 8). The app currently runs with in-memory data.
CREATE DATABASE IF NOT EXISTS tocas_db CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;
USE tocas_db;

CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(40) NOT NULL UNIQUE,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(120) NOT NULL UNIQUE,
  role_title VARCHAR(40) NOT NULL,
  room_name VARCHAR(80) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  salt VARCHAR(40) NOT NULL
);

CREATE TABLE appeals (            -- "tocas"
  id INT PRIMARY KEY,
  number VARCHAR(20) NOT NULL UNIQUE,
  plaintiff VARCHAR(120) NOT NULL,
  defendant VARCHAR(120) NOT NULL,
  origin_court VARCHAR(100) NOT NULL,
  received_date DATE NOT NULL,
  appeal_type VARCHAR(40) NOT NULL,
  origin_file VARCHAR(30) NOT NULL,
  room_name VARCHAR(80) NOT NULL,
  status ENUM('NEW','CONCLUDED') NOT NULL DEFAULT 'NEW',
  owner_id INT NOT NULL,
  concluded_date DATE NULL,
  FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE hearings (           -- "ponencias"
  id INT AUTO_INCREMENT PRIMARY KEY,
  appeal_id INT NOT NULL,
  judge_id INT NOT NULL,
  hearing_date DATE NOT NULL,
  hearing_time TIME NOT NULL,
  type ENUM('FIRST','SECOND','THIRD','ADDITIONAL') NOT NULL,
  status ENUM('UPCOMING','DONE') NOT NULL DEFAULT 'UPCOMING',
  auto_assigned BOOLEAN NOT NULL DEFAULT FALSE,
  FOREIGN KEY (appeal_id) REFERENCES appeals(id),
  FOREIGN KEY (judge_id) REFERENCES users(id)
);

CREATE TABLE resolutions (
  appeal_id INT PRIMARY KEY,
  status ENUM('DRAFT','FINALIZED') NOT NULL,
  version INT NOT NULL,
  resultando MEDIUMTEXT, considerando MEDIUMTEXT, resuelve MEDIUMTEXT,
  started_date DATE NOT NULL,
  last_modified DATETIME NOT NULL,
  finalized_at DATETIME NULL,
  pages INT NOT NULL DEFAULT 1,
  signed_file_name VARCHAR(255) NULL,
  signed_pages INT NULL,
  signed_size BIGINT NULL,
  signed_uploaded_at DATETIME NULL,
  signed_path VARCHAR(500) NULL,
  FOREIGN KEY (appeal_id) REFERENCES appeals(id)
);

CREATE TABLE notifications (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  appeal_id INT NULL,
  type VARCHAR(30) NOT NULL,
  title VARCHAR(120) NOT NULL,
  message VARCHAR(255) NOT NULL,
  created_at DATETIME NOT NULL,
  is_read BOOLEAN NOT NULL DEFAULT FALSE,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE audit_log (          -- "bitácora"
  id INT AUTO_INCREMENT PRIMARY KEY,
  appeal_id INT NOT NULL,
  created_at DATETIME NOT NULL,
  username VARCHAR(60) NOT NULL,
  action VARCHAR(60) NOT NULL,
  detail VARCHAR(255) NOT NULL,
  FOREIGN KEY (appeal_id) REFERENCES appeals(id)
);
