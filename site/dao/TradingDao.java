package crypto.site.dao;

import crypto.site.model.Holding;
import crypto.site.model.Transaction;
import crypto.site.websocket.WebSocketPriceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * TradingDao handles all database interactions related to trading operations,
 * including user balances, holdings, transactions, and account resets.
 */
@Repository
public class TradingDao {

    @Autowired
    private JdbcTemplate jdbc;

    public double getBalance(int userId) {
        return jdbc.queryForObject("SELECT balance FROM users WHERE id = ?", Double.class, userId);
    }

    public void updateBalance(int userId, double newBalance) {
        jdbc.update("UPDATE users SET balance = ? WHERE id = ?", newBalance, userId);
    }

    /**
     * Inserts a transaction (buy or sell) for a user,
     * and calculates profit/loss in case of a sell operation.
     *
     * @param type     Transaction type: "BUY" or "SELL".
     * @param pair     The trading pair, e.g., "ETH/USD".
     */
    public void insertTransaction(int userId, String type, String pair, double quantity, double price) {
        double profitLoss = 0;
        if ("SELL".equals(type)) {
            try {
                Map<String, Object> holding = jdbc.queryForMap("SELECT buy_price FROM holdings WHERE user_id = ? AND pair = ?", userId, pair);
                double buyPrice = (double) holding.get("buy_price");
                profitLoss = (price - buyPrice) * quantity;
            } catch (Exception e) {
                // In case buy_price is missing, assume no profitLoss
                profitLoss = 0.0;
            }
        }

        jdbc.update("INSERT INTO transactions (user_id, type, pair, quantity, price, profit_loss) VALUES (?, ?, ?, ?, ?, ?)",
                userId, type, pair, quantity, price, profitLoss);


        // Running cleanup after every transaction, in case quantity is 0
        jdbc.update("DELETE FROM holdings WHERE user_id = ? AND quantity = 0", userId);
    }

    /**
     * Updates the user's holdings for a specific pair. If the pair doesn't exist, it inserts a new one.
     * Handles both buy and sell operations.
     *
     * @param pair     The trading pair, e.g., "ETH/USD".
     */
    public void updateHoldings(int userId, String pair, double quantity, double price, boolean isBuy) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM holdings WHERE user_id = ? AND pair = ?", Integer.class, userId, pair);
        if (count != null && count > 0) {
            if (isBuy) {
                Map<String, Object> current = jdbc.queryForMap("SELECT quantity, buy_price FROM holdings WHERE user_id = ? AND pair = ?", userId, pair);
                BigDecimal qty = (BigDecimal) current.get("quantity");
                double currentQty = qty.doubleValue();
                double currentPrice = (double) current.get("buy_price");

                double newQty = currentQty + quantity;
                double newPrice = ((currentQty * currentPrice) + (quantity * price)) / newQty;

                if (Double.isInfinite(newQty) || Double.isNaN(newQty)) {
                    throw new IllegalArgumentException("Invalid quantity: " + newQty);
                }

                jdbc.update("UPDATE holdings SET quantity = ?, buy_price = ? WHERE user_id = ? AND pair = ?", newQty, newPrice, userId,  pair);
            } else {
                jdbc.update("UPDATE holdings SET quantity = quantity + ? WHERE user_id = ? AND pair = ?", quantity, userId,  pair);
            }
        } else {
            jdbc.update("INSERT INTO holdings (user_id, pair, quantity, buy_price) VALUES (?, ?, ?, ?)", userId, pair, quantity, price);
        }
    }

    /**
     * Retrieves all holdings for a user including calculated profit/loss
     * using current live prices from WebSocketPriceClient.
     */
    public List<Holding> getHoldings(int userId) {
        List<Holding> holdings = jdbc.query("SELECT * FROM holdings WHERE user_id = ?", new Object[]{userId}, (rs, rowNum) -> {
            Holding h = new Holding();
            h.setPair(rs.getString("pair"));
            h.setBuyPrice(rs.getDouble("buy_price"));
            h.setQuantity(rs.getDouble("quantity"));

            double currentPrice = WebSocketPriceClient.livePrices.getOrDefault(h.getPair(), 0.0);
            h.setCurrentPrice(currentPrice);

            double profitLoss = (currentPrice - h.getBuyPrice()) * h.getQuantity();
            h.setProfitLoss(profitLoss);

            return h;
        });

        return holdings;
    }

    /**
     * Retrieves all transactions for a user, ordered by most recent first.
     */
    public List<Transaction> getTransactions(int userId) {
        String sql = "SELECT pair, type, quantity, price, timestamp, profit_loss FROM transactions WHERE user_id = ? ORDER BY timestamp DESC";

        return jdbc.query(sql, new Object[]{userId}, new RowMapper<Transaction>() {
            @Override
            public Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {
                Transaction tx = new Transaction();
                tx.setPair(rs.getString("pair"));
                tx.setType(rs.getString("type"));
                tx.setQuantity(rs.getDouble("quantity"));
                tx.setPrice(rs.getDouble("price"));
                tx.setTimestamp(rs.getTimestamp("timestamp"));
                tx.setProfitLoss(rs.getObject("profit_loss") != null ? rs.getDouble("profit_loss") : null);
                return tx;
            }
        });
    }

    /**
     * FOR TESTING
     * Resets a user's account to initial state:
     * - Deletes all holdings and transactions
     * - Resets balance to $10,000
     */
    public void resetAccount(int userId) {
        jdbc.update("DELETE FROM holdings WHERE user_id = ?", userId);
        jdbc.update("DELETE FROM transactions WHERE user_id = ?", userId);
        jdbc.update("UPDATE users SET balance = 10000.0 WHERE id = ?", userId);
    }

    public double getHoldingQuantity(int userId, String pair) {
        List<Double> results = jdbc.query(
                "SELECT quantity FROM holdings WHERE user_id = ? AND pair = ?",
                new Object[]{userId, pair},
                (rs, rowNum) -> rs.getDouble("quantity")
        );
        return results.isEmpty() ? 0.0 : results.get(0);
    }

}

