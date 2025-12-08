package dao;
import exceptions.DataAccessException;
import model.Currency;
import model.ExchangeRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcExchangeRateDao implements ExchangeRateDao {

    private static final CurrencyDao dao = new JdbcCurrencyDao();
    private static final String sqlAllSelect = "SELECT ID, BaseCurrencyId, TargetCurrencyId, Rate FROM ExchangeRates;";
    private static final String sqlFindByPair = """
            SELECT er.ID, er.BaseCurrencyId, er.TargetCurrencyId, er.Rate
            FROM ExchangeRates AS er
            JOIN Currencies base ON er.BaseCurrencyId = base.ID
            JOIN Currencies target ON er.TargetCurrencyId = target.ID
            WHERE base.Code = ? AND target.Code = ?;
            """;
    private static final String sqlUpsert = """
            INSERT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate)
            VALUES (?,?,?)
            ON CONFLICT(BaseCurrencyId, TargetCurrencyId)
            DO UPDATE SET Rate = excluded.Rate;
            """;

    private static final Logger log = LoggerFactory.getLogger(JdbcExchangeRateDao.class);

    @Override
    public List<ExchangeRate> findAll() {
        log.debug("findAll() - get all exchangeRates");
        List<ExchangeRate> exchangeRates = new ArrayList<>();
        try(Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement()){
            ResultSet resultSet = statement.executeQuery(sqlAllSelect);
            while (resultSet.next()){
                exchangeRates.add(mapRow(resultSet));
            }
            log.info("findAll ExchangesRate found {}: ", exchangeRates.size());


        } catch (SQLException exception){
            log.error("error in db findAll ExchangeRates ", exception);
            throw new DataAccessException("failed to get list ExchangeCurrencies", exception);
        }
        return exchangeRates;
    }

    @Override
    public Optional<ExchangeRate> findByPair(String base, String target) {
        log.debug("findByPair() - get exchangeRate by pair");
        try(Connection connection = DatabaseConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sqlFindByPair)){
            preparedStatement.setString(1,base);
            preparedStatement.setString(2, target);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()){
                    ExchangeRate rate = mapRow(resultSet);
                    log.info("rate success found: {} {} = {}", base,target,rate.getRate());
                    return Optional.of(rate);
                } else {
                    log.warn("rate not found: {} {}",base, target );
                    return Optional.empty();
                }
            }
        } catch (SQLException exception){
            log.error("error in db search exchangeRate by pair {} {}",base, target, exception);
            throw new DataAccessException("error in search rate", exception);
        }
    }

    @Override
    public ExchangeRate save(ExchangeRate exchangeRate) {
        log.debug("save(rate={})",exchangeRate.getRate());
        try(Connection connection = DatabaseConnection.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sqlUpsert, Statement.RETURN_GENERATED_KEYS)){

            preparedStatement.setInt(1, exchangeRate.getBaseCurrency().getId());
            preparedStatement.setInt(2, exchangeRate.getTargetCurrency().getId());
            preparedStatement.setBigDecimal(3,exchangeRate.getRate());
            int rows = preparedStatement.executeUpdate();

            try(ResultSet keys = preparedStatement.getGeneratedKeys()){
                if (keys.next()){
                    exchangeRate.setId(keys.getInt(1));
                }
            }
            log.info("rate success saved (rows={}", rows);
            return exchangeRate;

        } catch(SQLException exception){
            log.error("error save", exception);
            throw new DataAccessException("not save rate", exception);
        }
    }


    private ExchangeRate mapRow(ResultSet rs) throws SQLException {
        ExchangeRate rate = new ExchangeRate();

        rate.setId(rs.getInt("ID"));
        rate.setRate(rs.getBigDecimal("Rate"));

        int baseId = rs.getInt("BaseCurrencyId");
        int targetId = rs.getInt("TargetCurrencyId");

        Currency baseCurrency = dao.findById(baseId).orElseThrow(() -> new SQLException("Currency not found, id: " + baseId));
        Currency targetCurrency = dao.findById(targetId).orElseThrow(() -> new SQLException("Currency not found, id: " + targetId));

        rate.setBaseCurrency(baseCurrency);
        rate.setTargetCurrency(targetCurrency);

        return rate;
    }
    }



