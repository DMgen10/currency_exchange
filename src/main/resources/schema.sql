CREATE TABLE Currencies
(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE ,
Code VARCHAR(3) NOT NULL UNIQUE,
FullName VARCHAR(100) NOT NULL,
Sign VARCHAR(5));

/* pragma foreign_keys = on; включу в java-код */

CREATE TABLE ExchangeRates
(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE ,
BaseCurrencyId INTEGER REFERENCES  Currencies(ID ),
TargetCurrencyId INTEGER REFERENCES Currencies(ID),
Rate DECIMAL(10,6)); /* был decimal(10,6) - сменили на real*/

CREATE UNIQUE INDEX idx_currencies_code ON Currencies (Code);
CREATE UNIQUE INDEX uk_exchange_rate_currency_pair ON ExchangeRates (BaseCurrencyId, TargetCurrencyId);

INSERT OR IGNORE INTO Currencies(Code, FullName, Sign) VALUES ('USD','United States dollar','$');
INSERT OR IGNORE INTO Currencies(Code, FullName, Sign) VALUES ('EUR','Euro','€');
INSERT OR IGNORE INTO Currencies(Code, FullName, Sign) VALUES ('RUB','Russian Ruble','₽');

/*курс на 31 октября*/
INSERT OR IGNORE INTO ExchangeRates(basecurrencyid, targetcurrencyid, rate) VALUES (1,2,80.98),/*USD*/
                                                                                   (1,3,0.867),
                                                                                   (2,1,1.15),/*EUR*/
                                                                                   (2,3,93.39),
                                                                                   (3,1,0.012349),/*RUB*/
                                                                                   (3,2,0.010708);


