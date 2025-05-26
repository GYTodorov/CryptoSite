package crypto.site.rest;

import crypto.site.services.TradingPairRegistry;
import crypto.site.services.TradingService;
import crypto.site.websocket.WebSocketPriceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class PriceController {

    @Autowired
    private TradingService tradingService;

    @Autowired
    private TradingPairRegistry tradingPairRegistry;

    @GetMapping("/prices")
    public ResponseEntity<Map<String, Double>> getPrices() {
        return ResponseEntity.ok(WebSocketPriceClient.livePrices);
    }

    @PostMapping("/buy")
    public ResponseEntity<String> buy(@RequestBody Map<String, Object> req) {
        String pair = (String) req.get("pair");
        double quantity = Double.parseDouble(req.get("quantity").toString());

        try {
            tradingService.buy(pair, quantity);
            return ResponseEntity.ok("Purchase successful");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<String> sell(@RequestBody Map<String, Object> req) {
        String pair = (String) req.get("pair");
        double quantity = Double.parseDouble(req.get("quantity").toString());
        try {
            tradingService.sell(pair, quantity);
            return ResponseEntity.ok("Sale successful");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<String> reset() {
        tradingService.resetAccount();
        return ResponseEntity.ok("Your balance has been reset to $10,000");
    }

    @GetMapping("/holdings")
    public ResponseEntity<?> holdings() {
        return ResponseEntity.ok(tradingService.getHoldings());
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> transactions() {
        return ResponseEntity.ok(tradingService.getTransactions());
    }

    @GetMapping("/balance")
    public ResponseEntity<Double> balance() {
        return ResponseEntity.ok(tradingService.getBalance());
    }

    @GetMapping("/top20cryptos")
    public ResponseEntity<List<String>> getTop20Cryptos() {
        return ResponseEntity.ok(tradingPairRegistry.getTop20Cryptos());
    }
}
