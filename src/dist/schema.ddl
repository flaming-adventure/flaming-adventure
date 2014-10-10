CREATE TABLE Booking
(
    ID INT PRIMARY KEY NOT NULL,
    Koie INT NOT NULL,
    Dato DATE NOT NULL,
    Navn VARCHAR(50) NOT NULL,
    Epost VARCHAR(50) NOT NULL,
    Antall INT DEFAULT 1 NOT NULL,
    Kommentar LONGTEXT,
    FOREIGN KEY (Koie) REFERENCES Koie (ID)
);
CREATE TABLE Ekstrautstyr
(
    ID INT PRIMARY KEY NOT NULL,
    Koie INT NOT NULL,
    Navn VARCHAR(20) NOT NULL,
    Innkjopt DATE,
    Antall INT DEFAULT 1 NOT NULL,
    FOREIGN KEY (Koie) REFERENCES Koie (ID)
);
CREATE TABLE Glemt
(
    ID INT PRIMARY KEY NOT NULL,
    Booking INT NOT NULL,
    Ting VARCHAR(20) NOT NULL,
    Levert TINYINT DEFAULT 0 NOT NULL,
    Kommentar LONGTEXT,
    FOREIGN KEY (Booking) REFERENCES Booking (ID)
);
CREATE TABLE Koie
(
    ID INT PRIMARY KEY NOT NULL,
    Navn VARCHAR(20) NOT NULL,
    Kapasitet INT NOT NULL,
    Ved INT NOT NULL
);
CREATE TABLE Odelagt
(
    ID INT PRIMARY KEY NOT NULL,
    Booking INT NOT NULL,
    Ting VARCHAR(20) NOT NULL,
    Fikset TINYINT DEFAULT 0 NOT NULL,
    FOREIGN KEY (Booking) REFERENCES Booking (ID)
);
CREATE TABLE Rapport
(
    ID INT PRIMARY KEY NOT NULL,
    Booking INT NOT NULL,
    Kommentar LONGTEXT,
    FOREIGN KEY (Booking) REFERENCES Booking (ID)
);
CREATE TABLE Ved
(
    ID INT PRIMARY KEY NOT NULL,
    Koie INT NOT NULL,
    Mengde INT NOT NULL,
    Dato DATE NOT NULL,
    FOREIGN KEY (Koie) REFERENCES Koie (ID)
);
