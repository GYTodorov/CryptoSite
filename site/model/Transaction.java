package crypto.site.model;

import java.sql.Timestamp;

public class Transaction {
    private String pair;
    private String type;
    private double quantity;
    private double price;
    private Timestamp timestamp;
    private Double profitLoss; // Can be null

    public Transaction() {
    }

    public Transaction(String pair, String type, double quantity, double price, Timestamp timestamp, Double profitLoss) {
        this.pair = pair;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
        this.profitLoss = profitLoss;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Double getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(Double profitLoss) {
        this.profitLoss = profitLoss;
    }
}
