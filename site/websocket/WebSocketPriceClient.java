package crypto.site.websocket;

import org.json.JSONArray;
import org.json.JSONObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A WebSocket client that subscribes to "Kraken's ticker updates" and updates live price map.
 */
public class WebSocketPriceClient extends WebSocketClient {
    public static final Map<String, Double> livePrices = new ConcurrentHashMap<>();

    private final String subscribeMessage;

    public WebSocketPriceClient(String subscribeMessage) throws URISyntaxException {
        super(new URI("wss://ws.kraken.com"));
        this.subscribeMessage = subscribeMessage;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        this.send(subscribeMessage);
    }

    @Override
    public void onMessage(String message) {
        if (!message.startsWith("[")) return;
        try {
            JSONArray jsonArr = new JSONArray(message);
            JSONObject ticker = jsonArr.getJSONObject(1);
            String pair = jsonArr.getString(3);
            double price = ticker.getJSONArray("c").getDouble(0);
            livePrices.put(pair, price);
        } catch (Exception e) {
            System.err.println("Failed to parse message: " + e.getMessage());
        }
    }

    @Override public void onClose(int code, String reason, boolean remote) {}
    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket Error: " + ex.getMessage());
    }
}

