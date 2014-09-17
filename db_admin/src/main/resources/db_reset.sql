DROP TABLE IF EXISTS Glemt;
DROP TABLE IF EXISTS Odelagt;
DROP TABLE IF EXISTS Rapport;
DROP TABLE IF EXISTS Booking;
DROP TABLE IF EXISTS Ved;
DROP TABLE IF EXISTS Ekstrautstyr;
DROP TABLE IF EXISTS Koie;

CREATE TABLE Koie (
    ID                      INT NOT NULL,
    Navn                    VARCHAR(20) NOT NULL,
    Kapasitet               INT NOT NULL,
    Ved                     INT NOT NULL,
    PRIMARY KEY (ID)
);

CREATE TABLE Ekstrautstyr (
    ID                      INT NOT NULL,
    Koie                    INT NOT NULL,
    Navn                    VARCHAR(20) NOT NULL,
    Innkjopt                DATE,
    Antall                  INT NOT NULL default '1',
    PRIMARY KEY (ID),
    FOREIGN KEY (koie) REFERENCES Koie(ID)
);

CREATE TABLE Ved (
    ID                      INT NOT NULL,
    Koie                    INT NOT NULL,
    Mengde                  INT NOT NULL,
    Dato                    DATE NOT NULL,
    PRIMARY KEY (ID),
    FOREIGN KEY (Koie) REFERENCES Koie(ID)
);

CREATE TABLE Booking (
    ID                      INT NOT NULL,
    Koie                    INT NOT NULL,
    Dato                    DATE NOT NULL,
    Navn                    VARCHAR(50) NOT NULL,
    Epost                   VARCHAR(50) NOT NULL,
    Antall                  INT NOT NULL default '1',
    Kommentar               TEXT,
    PRIMARY KEY (ID),
    FOREIGN KEY (Koie) REFERENCES Koie(ID)
);

CREATE TABLE Rapport (
    ID                      INT NOT NULL,
    Booking                 INT NOT NULL,
    Kommentar               TEXT,
    PRIMARY KEY (ID),
    FOREIGN KEY (Booking) REFERENCES Booking(ID)
);

CREATE TABLE Odelagt (
    ID                      INT NOT NULL,
    Booking                 INT NOT NULL,
    Ting                    VARCHAR(20) NOT NULL,
    Fikset                  BOOL NOT NULL default 0,
    PRIMARY KEY (ID),
    FOREIGN KEY (Booking) REFERENCES Booking(ID)
);

CREATE TABLE Glemt (
    ID                      INT NOT NULL,
    Booking                 INT NOT NULL,
    Ting                    VARCHAR(20) NOT NULL,
    Levert                  BOOL NOT NULL default 0,
    Kommentar               TEXT,
    PRIMARY KEY (ID),
    FOREIGN KEY (Booking) REFERENCES Booking(ID)
);

