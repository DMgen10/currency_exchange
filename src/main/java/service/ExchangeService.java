package service;

import dao.CurrencyDao;
import dao.ExchangeRateDao;
import exceptions.DataAccessException;
import model.Currency;
import model.ExchangeRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ExchangeService {

    private final Logger log = LoggerFactory.getLogger(ExchangeService.class);
    private final CurrencyDao currencyDao;
    private final ExchangeRateDao rateDao;

    public ExchangeService(CurrencyDao currencyDao, ExchangeRateDao rateDao) {
        this.currencyDao = Objects.requireNonNull(currencyDao, "currency cannot be null");
        this.rateDao = Objects.requireNonNull(rateDao, "rate cannot be null");
    }

    public List<ExchangeRate> getAllRates(){
        log.debug("getAllRates() {}", rateDao.findAll());
        List<ExchangeRate> exchangeRates = rateDao.findAll();
        log.info("getAllRates() - found exchangeRates: {}",exchangeRates.size() );
        return exchangeRates;
    }

    public BigDecimal convert(BigDecimal amount, String from, String to){
        validateAmount(amount);
        validateCurrencyCode(from,"current currency");
        validateCurrencyCode(to, "target currency");

        Currency baseCurrency = currencyDao.findByCode(from.toUpperCase()).orElseThrow(() -> new DataAccessException("currency no found " + from, null));
        Currency targetCurrency = currencyDao.findByCode(to.toUpperCase()).orElseThrow(() -> new DataAccessException("currency not found " + to, null));
        Optional<ExchangeRate> directRate = rateDao.findByPair(from, to);
        if (directRate.isPresent()){
            BigDecimal rate = directRate.get().getRate();
            return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        }
        Optional<ExchangeRate> reverseRate = rateDao.findByPair(to ,from);
        if (reverseRate.isPresent()){
            BigDecimal rate = reverseRate.get().getRate();
            return amount.divide(rate,10,RoundingMode.HALF_UP).setScale(2,RoundingMode.HALF_UP);
        }
        return calculateCrossRateViaUsd(amount,from,to);

    }

    public ExchangeRate addRate(String baseCode, String targetCode, BigDecimal rate){
        log.debug("addRate({},{},{})", baseCode,targetCode, rate);
        validateCurrencyCode(baseCode, "baseCurrency");
        validateCurrencyCode(targetCode, "targetCurrency");
        validateRate(rate);

        Currency base = currencyDao.findByCode(baseCode.toUpperCase()).orElseThrow(() -> new DataAccessException("currency not found" + baseCode, null));
        Currency target = currencyDao.findByCode(targetCode.toUpperCase()).orElseThrow(() -> new DataAccessException("currency not found" + targetCode, null));

        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setBaseCurrency(base);
        exchangeRate.setTargetCurrency(target);
        exchangeRate.setRate(rate);

        ExchangeRate saved = rateDao.save(exchangeRate);
        log.info("rate added: {} {} {}", baseCode, targetCode , rate);
        return saved;

    }

    public ExchangeRate getRate(String from, String to){
        validateCurrencyCode(from, "current currency ");
        validateCurrencyCode(to, "target currency");

        Optional<ExchangeRate> direct = rateDao.findByPair(from, to);
        if (direct.isPresent()){
            return direct.get();
        }

        Optional<ExchangeRate> reverse = rateDao.findByPair(to, from);

            if (reverse.isPresent()){
                ExchangeRate rate = reverse.get();
                ExchangeRate virtual = new ExchangeRate();
                virtual.setId(rate.getId());
                virtual.setBaseCurrency(rate.getTargetCurrency());
                virtual.setTargetCurrency(rate.getBaseCurrency());
                virtual.setRate(BigDecimal.ONE.divide(rate.getRate(),10, RoundingMode.HALF_UP));
                return virtual;
            }

        return null;
    }

    public Optional<ExchangeRate> getRateByPair(String baseCode, String targetCode){
        log.debug("getRateByPair({}, {}) ",baseCode, targetCode);
        validateCurrencyCode(baseCode, "baseCurrency");
        validateCurrencyCode(targetCode, "targetCurrency");
        return  rateDao.findByPair(baseCode.toUpperCase(), targetCode.toUpperCase());
    }

    public ExchangeRate updateRate(String baseCode, String targetCode, BigDecimal newRate){
        log.debug("updateRate({},{},{}) ", baseCode, targetCode, newRate);
        validateCurrencyCode(baseCode, "baseCurrency");
        validateCurrencyCode(targetCode, "targetCurrency");
        validateRate(newRate);

        ExchangeRate exists = getRateByPair(baseCode, targetCode).orElseThrow(() -> new DataAccessException("rate not found:  " + baseCode + targetCode, null));
        exists.setRate(newRate);
        ExchangeRate update = rateDao.save(exists);
        log.info("rate update: {} -> {} = {}", baseCode, targetCode, newRate);
        return update;
    }

    private BigDecimal calculateCrossRateViaUsd(BigDecimal amount, String from, String to){
        Currency currencyUsd = currencyDao.findByCode("USD").orElseThrow(() -> new DataAccessException("Base currency USD not found ", null));
        BigDecimal usdToFrom = rateDao.findByPair("USD",from).orElseThrow(() -> new DataAccessException("not found USD " + from, null)).getRate();
        BigDecimal usdToTarget = rateDao.findByPair("USD",to).orElseThrow(() -> new DataAccessException("not found USD " + to, null)).getRate();

        return amount.divide(usdToFrom, 10, RoundingMode.HALF_UP).multiply(usdToTarget).setScale(2,RoundingMode.HALF_UP);
    }

    private void validateAmount(BigDecimal amount){
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("amount must be positive "+ amount);
        }
    }
    private void validateCurrencyCode(String code, String field){
        if (code == null || code.trim().isEmpty()){
            throw new IllegalArgumentException("code cannot be empty " + code);
        }
    }

    private void validateRate(BigDecimal rate){
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("rate must be positive");
        }
    }


}
