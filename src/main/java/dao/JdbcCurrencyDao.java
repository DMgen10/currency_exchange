package dao;

import exceptions.DataAccessException;
import model.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCurrencyDao implements CurrencyDao{

    private static final Logger log = LoggerFactory.getLogger(JdbcCurrencyDao.class);
    private static final String sqlAllSelect = "SELECT ID, Code, Sign, FullName FROM Currencies;";
    private static final String sqlFindByCode = "SELECT ID, Code, Sign, FullName FROM Currencies WHERE Code = ?;";
    private static final String sqlByUpdate = """
    INSERT INTO Currencies (Code, FullName, Sign)
    ON CONFLICT(Code) DO UPDATE SET
    FullName = excluded.FullName,
    Sign = excluded.Sign;
   """;

    @Override
    public List<Currency> findAll() {
        log.debug("findAll() - get all currencies");
        List<Currency>currencies = new ArrayList<>();
        try(Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement()){
            ResultSet resultSet = statement.executeQuery(sqlAllSelect);
            while (resultSet.next()){
                currencies.add(mapRow(resultSet));
            }
            log.info("findAll() - currencies found: {}, ", currencies.size());
        } catch (SQLException exception){
            log.error("Error is search currencies ", exception);
            throw new DataAccessException("failed to get list currencies",exception);
        }
        return currencies;
    }

    @Override
    public Optional<Currency> findByCode(String code) {
        log.debug("findByCode() - get Currency by code");
        try(Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sqlFindByCode)){
            statement.setString(1,code);
            try(ResultSet resultSet = statement.executeQuery()){
                if (resultSet.next()){
                    Currency currency = mapRow(resultSet);
                    log.info("currency found: {} (ID={}", code, currency.getId());
                    return Optional.of(mapRow(resultSet));
                } else {
                    log.warn("currency not found: {} ", code);
                    return Optional.empty();
                }
            }
        } catch (SQLException exception){
            log.error("Error db is search currency by code ", exception);
            throw new DataAccessException("failed to search currency ", exception);
        }
    }

    @Override
    public Currency save(Currency currency) {

        log.debug("save(currency={})", currency.getCode());
        try(Connection connection = DatabaseConnection.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sqlByUpdate, PreparedStatement.RETURN_GENERATED_KEYS)){
            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getFullName());
            preparedStatement.setString(3, currency.getSign());
            int rows = preparedStatement.executeUpdate();

            try(ResultSet keys = preparedStatement.getGeneratedKeys()){
                if (keys.next()){
                    currency.setId(keys.getInt(1));
                }
            }
            log.info("currency {} success saved (rows={}", currency.getCode(), rows);
            return currency;
        } catch (SQLException exception){
            log.error("error saving currency {} ", currency.getCode(), exception);
            throw new DataAccessException("failed saving currency" + currency.getCode(), exception);
        }
    }

    private Currency mapRow(ResultSet resultSet) throws SQLException {
        Currency currency = new Currency();
        currency.setCode(resultSet.getString("Code"));
        currency.setId(resultSet.getInt("ID"));
        currency.setSign(resultSet.getString("Sign"));
        currency.setFullName(resultSet.getString("FullName"));
        return currency;
    }

    @Override
    public Optional<Currency> findById(int id) {
        String sql = "SELECT ID, Code, FullName, Sign FROM Currencies WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Currency currency = new Currency();
                    currency.setId(rs.getInt("ID"));
                    currency.setCode(rs.getString("Code"));
                    currency.setFullName(rs.getString("FullName"));
                    currency.setSign(rs.getString("Sign"));
                    return Optional.of(currency);
                }
            }
        } catch (SQLException exception) {
            log.error("Error finding currency by id: {}", id, exception);
        }
        return Optional.empty();
    }
}
