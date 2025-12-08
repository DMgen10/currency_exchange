package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.CurrencyDao;
import dao.JdbcCurrencyDao;
import exceptions.DataAccessException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Currency;
import service.CurrencyService;
import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = {
        "/currencies",
        "/currency/*"})
public class CurrencyServlet extends HttpServlet {

    private CurrencyService service;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init(){
        CurrencyDao dao = new JdbcCurrencyDao();
        service = new CurrencyService(dao);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                mapper.writeValue(resp.getWriter(), service.getAll());
            } else {
                String code = pathInfo.substring(1).toUpperCase();
                Currency currency = service.getByCode(code);
                if (currency == null) {
                    sendError(resp, 404, "currency not found");
                    return;
                }
                mapper.writeValue(resp.getWriter(), currency);
            }
        } catch (Exception e) {
            sendError(resp, 500, "server error");
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String name = req.getParameter("name");
            String code = req.getParameter("code");
            String sign = req.getParameter("sign");

            if (name == null || name.isBlank() || code == null || code.isBlank() || sign == null || sign.isBlank()) {
                sendError(resp, 400, "required from field is missing");
                return;
            }

            if (code.length() != 3) {
                sendError(resp, 400, "the currency code must be 3 chars long");
                return;
            }

            Currency currency = service.addCurrency(code.toUpperCase(), name, sign);
            resp.setStatus(201);
            mapper.writeValue(resp.getWriter(), currency);

        } catch (IllegalArgumentException exception) {
            sendError(resp, 400, exception.getMessage());
        } catch (DataAccessException exception) {
            sendError(resp, 409, exception.getMessage());
        } catch (Exception exception) {
            sendError(resp, 500, "server error");
        }
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException{
        resp.setStatus(status);
        mapper.writeValue(resp.getWriter(), Map.of("error", message));
    }

}
