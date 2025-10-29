CREATE TABLE Currencies
(ID INT PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE ,
Code VARCHAR(3),
FullName VARCHAR(50),
Sign VARCHAR(5));

/* pragma foreign_keys = on; включу в java-код */

CREATE TABLE ExchangeRate
(ID INT PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE ,
BaseCurrencyId INT REFERENCES Currencies(ID),
TargetCurrencyId INT REFERENCES Currencies(ID),
Rate REAL); /* был decimal(10,6) - сменили на real*/

INSERT OR IGNORE INTO Currencies(Code, FullName, Sign) VALUES ('USD','United States dollar','$');
INSERT OR IGNORE INTO Currencies(Code, FullName, Sign) VALUES ('EUR','Euro','€');
INSERT OR IGNORE INTO Currencies(Code, FullName, Sign) VALUES ('RUB','Russian Ruble','₽');


