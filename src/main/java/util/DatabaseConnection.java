package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlite:currency_exchange.db";;
        Connection connection = DriverManager.getConnection(url);

        try(Statement statement = connection.createStatement()){
            statement.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException exception){
            log.error("error db", exception);

        }
        return connection;
    }
}
