package crypto.site.services;

import crypto.site.dao.TradingDao;
import crypto.site.model.Holding;
import crypto.site.model.Transaction;
import crypto.site.websocket.WebSocketPriceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class that handles trading logic such as buying, selling, and balance management.
 */
@Service
public class TradingService {

    @Autowired
    private TradingDao dao;

    @Autowired
    private TradingPairRegistry pairRegistry;

    private final int userId = 1; // placeholder for auth

    /**
     * Validates a trade before execution.
     */
    public void validateTrade(String pair, double quantity, boolean isBuy) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("You can't buy or sell 0 or negative quantity");
        }

        if (!pairRegistry.isValid(pair)) {
            throw new IllegalArgumentException("Invalid trading pair: " + pair);
        }

        if (!isBuy) {
            double currentHoldingQty = dao.getHoldingQuantity(userId, pair);

            if (quantity > currentHoldingQty) {
                throw new IllegalArgumentException("You can't sell more than you own");
            }
        }
    }

    public void buy(String pair, double quantity) {
        boolean isBuy = true;
        validateTrade(pair, quantity, isBuy);

        double price = WebSocketPriceClient.livePrices.getOrDefault(pair, 0.0);
        double cost = price * quantity;

        double balance = dao.getBalance(userId);
        if (cost > balance) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        dao.updateBalance(userId, balance - cost);
        dao.updateHoldings(userId, pair, quantity, price, isBuy);
        dao.insertTransaction(userId, "BUY", pair, quantity, price);
    }

    public void sell(String pair, double quantity) {
        boolean isBuy = false;
        validateTrade(pair, quantity, isBuy);

        double price = WebSocketPriceClient.livePrices.getOrDefault(pair, 0.0);
        double proceeds = price * quantity;

        // skipping actual holding check for simplicity here
        dao.updateBalance(userId,dao.getBalance(userId) + proceeds);
        dao.updateHoldings(userId, pair, -quantity, price, isBuy);
        dao.insertTransaction(userId, "SELL", pair, quantity, price);
    }

    public List<Holding> getHoldings() {
        return dao.getHoldings(userId);
    }

    public List<Transaction> getTransactions() {
        return dao.getTransactions(userId);
    }

    public double getBalance() {
        return dao.getBalance(userId);
    }

    public void resetAccount() {
        dao.resetAccount(userId);
    }


}
