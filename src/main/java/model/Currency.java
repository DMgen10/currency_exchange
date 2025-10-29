package model;

public class Currency {

    private long id;          // айди валюты
    private String code;      // код валюты
    private String sign;      // знак валюты
    private String fullName;  // Полное имя валюты

    public Currency(long id, String code, String sign, String fullName) {
        this.id = id;
        this.code = code;
        this.sign = sign;
        this.fullName = fullName;
    }

    public String getSign() {
        return sign;
    }

    public String getFullName() {
        return fullName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
