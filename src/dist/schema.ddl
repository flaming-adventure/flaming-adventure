CREATE TABLE broken_items
(
    id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    hut_id INT NOT NULL,
    item VARCHAR(24) NOT NULL,
    date DATE,
    fixed TINYINT DEFAULT 0 NOT NULL,
    comment LONGTEXT,
    FOREIGN KEY (hut_id) REFERENCES huts (id)
);
CREATE TABLE equipment
(
    id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    hut_id INT NOT NULL,
    name VARCHAR(24) NOT NULL,
    purchase_date DATE,
    count INT DEFAULT 1 NOT NULL,
    FOREIGN KEY (hut_id) REFERENCES huts (id)
);
CREATE TABLE forgotten_items
(
    id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    hut_id INT NOT NULL,
    item VARCHAR(24) NOT NULL,
    name VARCHAR(64) NOT NULL,
    contact VARCHAR(64) NOT NULL,
    date DATE,
    delivered TINYINT DEFAULT 0 NOT NULL,
    comment LONGTEXT,
    FOREIGN KEY (hut_id) REFERENCES huts (id)
);
CREATE TABLE huts
(
    id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    name VARCHAR(24) NOT NULL,
    capacity INT NOT NULL,
    firewood INT DEFAULT 0 NOT NULL
);
CREATE TABLE reservations
(
    id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    hut_id INT NOT NULL,
    date DATE NOT NULL,
    name VARCHAR(64) NOT NULL,
    email VARCHAR(64) NOT NULL,
    count INT DEFAULT 1 NOT NULL,
    comment LONGTEXT,
    FOREIGN KEY (hut_id) REFERENCES huts (id)
);
