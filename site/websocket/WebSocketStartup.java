package crypto.site.websocket;

import crypto.site.services.TradingPairRegistry;
import org.java_websocket.client.WebSocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Component that initializes WebSocket connection to Kraken once the application starts.
 */
@Component
public class WebSocketStartup {

    @Autowired
    private TradingPairRegistry tradingPairRegistry;

    /**
     * Initializes and connects the WebSocket client with the top 20 pairs on application startup.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        try {
            List<String> top20 = tradingPairRegistry.getTop20Cryptos();

            List<String> formattedPairs = top20.stream()
                    .map(symbol -> symbol + "/USD")
                    .toList();

            String subscribeMessage = buildSubscribeMessage(formattedPairs);

            WebSocketClient client = new WebSocketPriceClient(subscribeMessage);

            client.connect();

            System.out.println("WebSocket started with top 20 pairs.");
        } catch (Exception e) {
            System.err.println("Error starting WebSocket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Builds a JSON string for subscribing to Kraken ticker data.
     */
    private String buildSubscribeMessage(List<String> pairs) {
        String pairList = pairs.stream()
                .map(p -> "\"" + p + "\"")
                .reduce((a, b) -> a + "," + b)
                .orElse("");

        return """
        {
          "event":"subscribe",
          "pair":[%s],
          "subscription":{"name":"ticker"}
        }
        """.formatted(pairList);
    }
}
