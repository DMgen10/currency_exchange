package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class InitDatabase {

    private static final Logger log = LoggerFactory.getLogger(InitDatabase.class);

    public static void initialize(){

        try(Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement()){
            statement.execute("""
                CREATE TABLE IF NOT EXISTS Currencies
                (ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE,
                Code VARCHAR(3) NOT NULL UNIQUE,
                FullName VARCHAR(100) NOT NULL,
                Sign VARCHAR(5));""");
            statement.execute("""
                CREATE TABLE IF NOT EXISTS ExchangeRates
                (ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE ,
                BaseCurrencyId INTEGER NOT NULL REFERENCES Currencies(ID),
                TargetCurrencyId INTEGER NOT NULL REFERENCES Currencies(ID),
                Rate REAL NOT NULL);""");
            statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_currencies_code ON Currencies (Code);");
            statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_exchange_rate_currency_pair ON ExchangeRates (BaseCurrencyId, TargetCurrencyId);");
            statement.execute("INSERT OR IGNORE INTO Currencies(Code, FullName, Sign) VALUES ('USD','United States dollar','$');");
            statement.execute("INSERT OR IGNORE INTO Currencies(Code, FullName, Sign) VALUES ('EUR','Euro','€');");
            statement.execute("INSERT OR IGNORE INTO Currencies(Code, FullName, Sign) VALUES ('RUB','Russian Ruble','₽');");
            statement.execute("""
                INSERT OR IGNORE INTO ExchangeRates(basecurrencyid, targetcurrencyid, rate) VALUES 
                       (1,2,0.8662),/*USD*/
                       (1,3,80.8861),
                       (2,1,1.15),/*EUR*/
                       (2,3,93.39),
                       (3,1,0.012349),/*RUB*/
                       (3,2,0.010708);
                """);

            System.out.println("db init success");
        } catch (SQLException exception){
            log.error("error db", exception);
        }

    }
}
