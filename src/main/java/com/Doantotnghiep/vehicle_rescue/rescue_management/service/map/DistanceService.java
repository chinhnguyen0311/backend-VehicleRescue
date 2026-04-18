package com.Doantotnghiep.vehicle_rescue.rescue_management.service.map;

import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.MechanicSearchResultDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DistanceService {
    private static final String ORS_URL = "https://api.openrouteservice.org/v2/matrix/driving-car";
    private static final String API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjcyZDk2YjZjYWMxNDQ2MzFhODFiYjBhNDQ3NTBjMWZlIiwiaCI6Im11cm11cjY0In0=";

    public List<Double> getRealDistances(
            double originLat,
            double originLng,
            List<MechanicSearchResultDTO> mechanics
    ) {
        RestTemplate restTemplate = new RestTemplate();

        // build locations
        List<List<Double>> locations = new ArrayList<>();

        // origin
        locations.add(List.of(originLng, originLat));

        // destinations
        for (MechanicSearchResultDTO m : mechanics) {
            if (m.getLatitude() != null && m.getLongitude() != null) {
                locations.add(List.of(m.getLongitude(), m.getLatitude()));
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("locations", locations);
        body.put("metrics", List.of("distance"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        Map response = restTemplate.postForObject(ORS_URL, entity, Map.class);

        List<List<Double>> distances = (List<List<Double>>) response.get("distances");

        List<Double> result = new ArrayList<>();

        List<Double> firstRow = distances.get(0);

        for (int i = 1; i < firstRow.size(); i++) {
            result.add(firstRow.get(i) / 1000.0); // m → km
        }

        return result;
    }
    public Double getDistance(Double fromLat, Double fromLng,
                              Double toLat, Double toLng) {
        try {
            String url = "https://api.openrouteservice.org/v2/directions/driving-car";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", API_KEY);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = """
        {
          "coordinates": [[%f,%f],[%f,%f]]
        }
        """.formatted(fromLng, fromLat, toLng, toLat);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            var routes = (List<Map>) response.getBody().get("routes");
            var summary = (Map) routes.get(0).get("summary");

            Double distanceMeter = (Double) summary.get("distance");
            return distanceMeter / 1000; // km

        } catch (Exception e) {
            return null;
        }
    }
    public String getAddress(Double lat, Double lng) {
        try {
            String url = "https://api.openrouteservice.org/geocode/reverse?api_key="
                    + API_KEY + "&point.lon=" + lng + "&point.lat=" + lat;
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            var features = (List<Map>) response.getBody().get("features");
            var properties = (Map) features.get(0).get("properties");

            String label = (String) properties.get("label");

            return cleanLabel(label);

        } catch (Exception e) {
            return "Không xác định";
        }
    }
    private String cleanLabel(String label) {
        if (label == null || label.isBlank()) return "Không xác định";

        // Chuẩn hóa trước
        label = label
                .replace("Ha Noi", "Hà Nội")
                .replace("Hanoi", "Hà Nội")
                .replace("HI", "Hà Nội")
                .replace("Ho Chi Minh City", "TP. Hồ Chí Minh")
                .replace(", Vietnam", "")
                .replaceAll(",\\s*,", ",") // fix ", ,"
                .trim();

        // 🔥 Tách ra để remove trùng
        String[] parts = label.split(",");
        List<String> uniqueParts = new ArrayList<>();

        for (String part : parts) {
            String trimmed = part.trim();
            if (!uniqueParts.contains(trimmed) && !trimmed.isEmpty()) {
                uniqueParts.add(trimmed);
            }
        }

        return String.join(", ", uniqueParts);
    }
    public double[] getCoordinatesFromAddress(String address) {
        try {
            String geoAddress = normalizeForGeocode(address);

            String url = "https://api.openrouteservice.org/geocode/search?api_key="
                    + API_KEY
                    + "&text=" + URLEncoder.encode(geoAddress, StandardCharsets.UTF_8)
                    + "&size=1"
                    + "&boundary.country=VN";

            RestTemplate restTemplate = new RestTemplate();
            Map response = restTemplate.getForObject(url, Map.class);

            var features = (List<Map>) response.get("features");
            if (features == null || features.isEmpty()) return null;

            var geometry = (Map) features.get(0).get("geometry");
            var coordinates = (List<Double>) geometry.get("coordinates");

            return new double[]{coordinates.get(1), coordinates.get(0)};

        } catch (Exception e) {
            return null;
        }
    }
    private String normalizeForGeocode(String address) {
        if (address == null) return null;

        address = removeAccent(address);

        return address
                .replace("pho", "")
                .replace("duong", "")
                .replace("ngo", "")
                .replace("ngach", "")
                .replaceAll("\\s+", " ")
                .trim();
    }
    private String removeAccent(String input) {
        if (input == null) return null;

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
