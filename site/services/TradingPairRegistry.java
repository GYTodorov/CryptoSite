package crypto.site.services;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for interacting with the Kraken API to retrieve trading pairs.
 * It caches the result and maintains a list of valid trading pairs and top 20 cryptocurrencies.
 */
@Service
public class TradingPairRegistry {

    private final RestTemplate restTemplate = new RestTemplate();

    private Map<String, Object> cachedResult = new HashMap<>();
    private List<String> top20Cryptos = new ArrayList<>();
    private final Set<String> validPairs = new HashSet<>();

    @PostConstruct
    public void init() {
        fetchAndCacheKrakenData();
    }

    public void fetchAndCacheKrakenData() {
        String url = "https://api.kraken.com/0/public/AssetPairs";
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !response.containsKey("result")) {
            throw new IllegalStateException("Failed to fetch Kraken data");
        }

        cachedResult = (Map<String, Object>) response.get("result");

        validPairs.clear();
        for (Map.Entry<String, Object> entry : cachedResult.entrySet()) {
            String pairName = entry.getKey(); // e.g. "XBTUSD", "ETHUSD", etc.
            validPairs.add(pairName.toUpperCase()); // normalize case
        }

        computeTop20Cryptos();
    }

    private void computeTop20Cryptos() {
        Map<String, Integer> baseCount = new HashMap<>();
        for (Map.Entry<String, Object> entry : cachedResult.entrySet()) {
            Map<String, Object> pairInfo = (Map<String, Object>) entry.getValue();
            String base = (String) pairInfo.get("base");
            if (base != null) {
                baseCount.put(base, baseCount.getOrDefault(base, 0) + 1);
            }
        }
        top20Cryptos = baseCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .map(entry -> normalizeAssetName(entry.getKey()))
                .collect(Collectors.toList());
    }

    public List<String> getTop20Cryptos() {
        return top20Cryptos;
    }

    /**
     * Normalizes Kraken asset names by removing leading 'X' or 'Z' if present.
     */
    private String normalizeAssetName(String krakenAsset) {
        if (krakenAsset.startsWith("X") || krakenAsset.startsWith("Z")) {
            return krakenAsset.substring(1);
        }
        return krakenAsset;
    }

    /**
     * Checks if a given pair (e.g., "ETH/USD") is valid
     */
    public boolean isValid(String pair) {
        // Normalize from "BCH/USD" to "BCHUSD" style
        String normalized = pair.replace("/", "").toUpperCase();
        return validPairs.contains(normalized);
    }

    public Set<String> getAllPairs() {
        return validPairs;
    }
}
