    package servlet;

    import com.fasterxml.jackson.databind.ObjectMapper;
    import dao.CurrencyDao;
    import dao.ExchangeRateDao;
    import dao.JdbcCurrencyDao;
    import dao.JdbcExchangeRateDao;
    import dto.PatchRateRequest;
    import exceptions.DataAccessException;
    import jakarta.servlet.ServletConfig;
    import jakarta.servlet.ServletException;
    import jakarta.servlet.annotation.WebServlet;
    import jakarta.servlet.http.HttpServlet;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import model.ExchangeRate;
    import service.ExchangeService;
    import java.io.IOException;
    import java.math.BigDecimal;
    import java.util.HashMap;
    import java.util.Map;

    @WebServlet(urlPatterns = {
            "/exchangeRates",
            "/exchangeRate/*"})
    public class ExchangeRateServlet extends HttpServlet {

        private ExchangeService service;
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public void init(ServletConfig config) throws ServletException {
            CurrencyDao currencyDao = new JdbcCurrencyDao();
            ExchangeRateDao rateDao = new JdbcExchangeRateDao();
            service = new ExchangeService(currencyDao, rateDao);
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            String pathInfo = req.getPathInfo();

            try {
                if (pathInfo == null || "/".equals(pathInfo)) {
                    mapper.writeValue(resp.getWriter(), service.getAllRates());
                } else {
                    String pair = pathInfo.substring(1).toUpperCase();
                    if (pair.length() != 6) {
                        sendError(resp, 400, "The currency pair must consist of 6 characters");
                        return;
                    }
                    String base = pair.substring(0, 3);
                    String target = pair.substring(3);

                    ExchangeRate rate = service.getRateByPair(base, target).orElseThrow(() -> new DataAccessException("exchange rate for a currency pair " + pair + " not found", null));

                    mapper.writeValue(resp.getWriter(), rate);
                }
            } catch (DataAccessException e) {
                sendError(resp, 404, e.getMessage());
            } catch (Exception e) {
                sendError(resp, 500, "server error");
            }
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            try {
                String baseCurrencyCode = req.getParameter("baseCurrencyCode");
                String targetCurrencyCode = req.getParameter("targetCurrencyCode");
                String rateStr = req.getParameter("rate");

                if (baseCurrencyCode == null || baseCurrencyCode.isBlank() ||
                        targetCurrencyCode == null || targetCurrencyCode.isBlank() ||
                        rateStr == null || rateStr.isBlank()) {
                    sendError(resp, 400, "a required form field is missing");
                    return;
                }

                if (baseCurrencyCode.length() != 3 || targetCurrencyCode.length() != 3) {
                    sendError(resp, 400, "currency codes must be 3 characters long");
                    return;
                }

                BigDecimal rate;
                try {
                    rate = new BigDecimal(rateStr);
                    if (rate.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    sendError(resp, 400, "incorrect value rate");
                    return;
                }

                ExchangeRate rateEntity = service.addRate(baseCurrencyCode.toUpperCase(), targetCurrencyCode.toUpperCase(), rate);
                resp.setStatus(201);
                mapper.writeValue(resp.getWriter(), rateEntity);

            } catch (IllegalArgumentException e) {
                sendError(resp, 400, e.getMessage());
            } catch (DataAccessException e) {
                if (e.getMessage().contains("not found") || e.getMessage().contains("already exists")) {
                    sendError(resp, 404, e.getMessage());
                } else if (e.getMessage().contains("already exists") || e.getMessage().contains("already exists")) {
                    sendError(resp, 409, e.getMessage());
                } else {
                    sendError(resp, 500, e.getMessage());
                }
            } catch (Exception e) {
                sendError(resp, 500, "internal server error");
            }
        }

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

            String method = req.getMethod();
            if ("PATCH".equalsIgnoreCase(method)) {
                doPatch(req, resp);
            } else if ("OPTIONS".equalsIgnoreCase(method)) {
                resp.setHeader("Allow", "GET, POST, PATCH, OPTIONS");
                resp.setStatus(200);
            } else {
                super.service(req, resp);
            }
        }

        private void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() != 7) {
                sendError(resp, 400, "The currency pair is missing from the address or is incorrect");
                return;
            }

            String codePair = pathInfo.substring(1).toUpperCase();
            String baseCurrencyCode = codePair.substring(0, 3);
            String targetCurrencyCode = codePair.substring(3);

            try {
                PatchRateRequest request = mapper.readValue(req.getInputStream(), PatchRateRequest.class);
                BigDecimal rate = request.getRate();

                if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
                    sendError(resp, 400, "incorrect value rate");
                    return;
                }

                ExchangeRate updated = service.updateRate(baseCurrencyCode, targetCurrencyCode, rate);
                mapper.writeValue(resp.getWriter(), updated);
            } catch (DataAccessException e) {
                sendError(resp, 404, "The currency pair is not in the database");
            } catch (Exception e) {
                sendError(resp, 500, "server error");
            }
        }
        private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
            resp.setStatus(status);
            Map<String, String> error = new HashMap<>();
            error.put("message", message);
            mapper.writeValue(resp.getWriter(), error);
        }
    }
