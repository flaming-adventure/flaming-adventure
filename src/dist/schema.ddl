CREATE TABLE huts
(
  id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  name VARCHAR(20) NOT NULL,
  capacity INT NOT NULL,
  firewood INT NOT NULL
);
CREATE TABLE reservations
(
    id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    hut_id INT NOT NULL,
    date DATE NOT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL,
    count INT DEFAULT 1 NOT NULL,
    comment LONGTEXT,
    FOREIGN KEY (hut_id) REFERENCES huts (id)
);
CREATE TABLE equipment
(
    id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    hut_id INT NOT NULL,
    name VARCHAR(20) NOT NULL,
    purchase_date DATE,
    count INT DEFAULT 1 NOT NULL,
    FOREIGN KEY (hut_id) REFERENCES huts (id)
);
CREATE TABLE forgotten_items
(
    id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    reservation_id INT NOT NULL,
    item VARCHAR(20) NOT NULL,
    delivered TINYINT DEFAULT 0 NOT NULL,
    comment LONGTEXT,
    FOREIGN KEY (reservation_id) REFERENCES reservations (id)
);

CREATE TABLE out_of_order
(
    id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    reservation_id INT NOT NULL,
    item VARCHAR(20) NOT NULL,
    fixed TINYINT DEFAULT 0 NOT NULL,
    FOREIGN KEY (reservation_id) REFERENCES reservations (id)
);
CREATE TABLE reports
(
    id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    reservation_id INT NOT NULL,
    comment LONGTEXT,
    FOREIGN KEY (reservation_id) REFERENCES reservations (id)
);
CREATE TABLE firewood_log
(
    id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    hut_id INT NOT NULL,
    amount INT NOT NULL,
    date DATE NOT NULL,
    FOREIGN KEY (hut_id) REFERENCES huts (id)
);
