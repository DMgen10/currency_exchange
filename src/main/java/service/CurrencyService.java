package service;

import dao.CurrencyDao;
import exceptions.DataAccessException;
import model.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class CurrencyService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyService.class);
    private final CurrencyDao dao;

    public CurrencyService(CurrencyDao dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    public List<Currency> getAll(){
        log.debug("getAll()");
        List<Currency> currencies = dao.findAll();
        log.info("getAll() - found currencies: {}", currencies.size());
        return currencies;
    }

    public Currency getCurrencyByCode(String code){
        log.debug("getCurrencyByCode() {}", code);
        String currentCode = validationCurrencyCode(code);

        return dao.findByCode(code).orElseThrow(() -> {
            log.warn("Currency not found: {}", currentCode);
            return new DataAccessException("Currency not found: " + currentCode, null);
        });
    }

    public Currency getByCode(String code) {
        return dao.findByCode(code.toUpperCase()).orElseThrow(() -> new DataAccessException("currency with code " + code + " not found", null));
    }

    public Currency addCurrency(String code, String name, String sign){
        log.debug("addCurrency(code={}, name={}, sign={})", code, name, sign);
        String validCode = validationCurrencyCode(code);
        validateCurrencyName(name);
        validateSign(sign);

        if (dao.findByCode(validCode).isPresent()){
            throw new DataAccessException("Currency is code " + validCode + " already exists" ,null);
        }

        Currency newCurrency = new Currency();
        newCurrency.setCode(validCode);
        newCurrency.setFullName(name.trim());
        newCurrency.setSign(sign.trim());

        Currency saved = dao.save(newCurrency);
        log.info("Currency added: {} (ID = {})",validCode, saved.getId());
        return saved;
    }

    private String validationCurrencyCode(String code){
        if (code == null || code.trim().isEmpty()){
            throw new IllegalArgumentException("code cannot be empty");
        }
        return code.trim().toUpperCase();
    }

    private void validateCurrencyName(String name){
        if (name == null || name.trim().isEmpty()){
            throw new IllegalArgumentException("name cannot be empty");
        }
        if (name.trim().length() > 100){
            throw new IllegalArgumentException("name length cannot 100");
        }
    }

    private void validateSign(String sign){
        if (sign == null || sign.trim().isEmpty()){
            throw new IllegalArgumentException("sign cannot bt empty");
        }
        if (sign.trim().length() > 5){
            throw new IllegalArgumentException("sing length cannot be 5");
        }
    }
}
