package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dao.JdbcCurrencyDao;
import dao.JdbcExchangeRateDao;
import dto.ExchangeResponse;
import exceptions.DataAccessException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExchangeService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {

    private ExchangeService service;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init() {
        CurrencyDao currencyDao = new JdbcCurrencyDao();
        ExchangeRateDao exchangeRateDao = new JdbcExchangeRateDao();
        service = new ExchangeService(currencyDao, exchangeRateDao);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String from = req.getParameter("from");
            String to = req.getParameter("to");
            String amountString = req.getParameter("amount");

            if (from == null || from.trim().isEmpty()){
                sendError(resp, 400, "parameter from mandatory");
                return;
            }

            if (to == null || to.trim().isEmpty()){
                sendError(resp, 400, "parameter to mandatory");
                return;
            }
            if (amountString == null || amountString.trim().isEmpty()){
                sendError(resp, 400, "parameter amount mandatory");
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amountString);
                if (amount.compareTo(BigDecimal.ZERO) <= 0){
                    sendError(resp, 400, " sum must be > 0");
                    return;
                }
            } catch (NumberFormatException exception){
                sendError(resp, 400, "incorrect format sum: " + amountString);
                return;
            }
            BigDecimal convertedAmount = service.convert(amount, from.toUpperCase(), to.toUpperCase());
            ExchangeResponse response = new ExchangeResponse();
            response.setFrom(from.toUpperCase());
            response.setTo(to.toUpperCase());
            response.setAmount(amount);
            response.setConvertAmount(convertedAmount);

            mapper.writeValue(resp.getWriter(), response);

        } catch (IllegalArgumentException | DataAccessException exception){
            sendError(resp,  400, exception.getMessage());
        } catch (Exception exception){
            sendError(resp, 500, "error converting " + exception.getMessage());
        }
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        Map<String,String> error = new HashMap<>();
        error.put("error", message);
        mapper.writeValue(resp.getWriter(), error);
    }
}
