package dao;

import model.Currency;
import java.util.List;
import java.util.Optional;

public interface CurrencyDao {

     List<Currency> findAll();

     Optional<Currency> findByCode(String code);

     Currency save(Currency currency);

     Optional<Currency> findById(int id);

}
