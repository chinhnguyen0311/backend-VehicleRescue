package com.Doantotnghiep.vehicle_rescue.rescue_management.service.map;

import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.MechanicSearchResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class DistanceServiceTest {

    private DistanceService distanceService;

    @BeforeEach
    void setUp() {
        distanceService = new DistanceService();
    }

    // =======================================================================
    // Test getRealDistances (Matrix API)
    // =======================================================================

    @Test
    void getRealDistances_success() {
        // Giả lập DTO thợ máy
        MechanicSearchResultDTO m1 = new MechanicSearchResultDTO();
        m1.setLatitude(21.1); m1.setLongitude(105.1);
        MechanicSearchResultDTO m2 = new MechanicSearchResultDTO();
        m2.setLatitude(21.2); m2.setLongitude(105.2);
        List<MechanicSearchResultDTO> mechanics = Arrays.asList(m1, m2);

        // Giả lập RestTemplate khởi tạo nội bộ
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, context) -> {
            // Giả lập JSON response từ OpenRouteService[cite: 8]
            Map<String, Object> responseMap = new HashMap<>();
            List<List<Double>> distances = new ArrayList<>();
            // Phần tử 0 là khoảng cách từ origin -> origin (0)
            // Phần tử 1 là origin -> m1 (5000m)
            // Phần tử 2 là origin -> m2 (10500m)
            distances.add(Arrays.asList(0.0, 5000.0, 10500.0));
            responseMap.put("distances", distances);

            when(mock.postForObject(anyString(), any(HttpEntity.class), eq(Map.class))).thenReturn(responseMap);
        })) {
            List<Double> result = distanceService.getRealDistances(21.0, 105.0, mechanics);

            assertEquals(2, result.size());
            assertEquals(5.0, result.get(0));   // 5000m -> 5km
            assertEquals(10.5, result.get(1));  // 10500m -> 10.5km
        }
    }

    // =======================================================================
    // Test getDistance (Directions API)
    // =======================================================================

    @Test
    void getDistance_success() {
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, context) -> {
            Map<String, Object> summary = new HashMap<>();
            summary.put("distance", 15000.0); // 15,000 meters

            Map<String, Object> route = new HashMap<>();
            route.put("summary", summary);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("routes", Collections.singletonList(route));

            ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
            when(mock.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class))).thenReturn(responseEntity);
        })) {
            Double distance = distanceService.getDistance(21.0, 105.0, 21.1, 105.1);
            assertEquals(15.0, distance); // 15km
        }
    }

    @Test
    void getDistance_throwsException_returnsNull() {
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, context) -> {
            // Giả lập lỗi gọi API[cite: 8]
            when(mock.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                    .thenThrow(new RestClientException("API Error"));
        })) {
            Double distance = distanceService.getDistance(21.0, 105.0, 21.1, 105.1);
            assertNull(distance);
        }
    }

    // =======================================================================
    // Test getAddress (Reverse Geocoding API)
    // =======================================================================

    @Test
    void getAddress_success_cleansLabelCorrectly() {
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, context) -> {
            Map<String, Object> properties = new HashMap<>();
            // Giả lập nhãn bị trùng lặp và có cụm từ chưa chuẩn hóa theo logic của bạn[cite: 8]
            properties.put("label", "Đường Xuân Thủy, Cầu Giấy, Ha Noi, Ha Noi, Vietnam");

            Map<String, Object> feature = new HashMap<>();
            feature.put("properties", properties);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("features", Collections.singletonList(feature));

            ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
            when(mock.getForEntity(anyString(), eq(Map.class))).thenReturn(responseEntity);
        })) {
            String address = distanceService.getAddress(21.0, 105.0);

            // "Ha Noi" được đổi thành "Hà Nội", "Vietnam" bị bỏ, và các mục trùng lặp bị xóa[cite: 8]
            assertEquals("Đường Xuân Thủy, Cầu Giấy, Hà Nội", address);
        }
    }

    @Test
    void getAddress_throwsException_returnsDefaultString() {
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, context) -> {
            when(mock.getForEntity(anyString(), eq(Map.class)))
                    .thenThrow(new RestClientException("API down"));
        })) {
            String address = distanceService.getAddress(21.0, 105.0);
            assertEquals("Không xác định", address); // Exception sẽ fallback về "Không xác định"[cite: 8]
        }
    }

    // =======================================================================
    // Test getCoordinatesFromAddress (Geocoding API)
    // =======================================================================

    @Test
    void getCoordinatesFromAddress_success() {
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, context) -> {
            Map<String, Object> geometry = new HashMap<>();
            geometry.put("coordinates", Arrays.asList(105.0, 21.0)); // [longitude, latitude][cite: 8]

            Map<String, Object> feature = new HashMap<>();
            feature.put("geometry", geometry);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("features", Collections.singletonList(feature));

            when(mock.getForObject(anyString(), eq(Map.class))).thenReturn(responseMap);
        })) {
            double[] coords = distanceService.getCoordinatesFromAddress("Đại học Giao thông vận tải, Hà Nội");

            assertNotNull(coords);
            assertEquals(21.0, coords[0]); // latitude[cite: 8]
            assertEquals(105.0, coords[1]); // longitude[cite: 8]
        }
    }

    @Test
    void getCoordinatesFromAddress_emptyFeatures_returnsNull() {
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, context) -> {
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("features", Collections.emptyList()); // API không tìm thấy tọa độ

            when(mock.getForObject(anyString(), eq(Map.class))).thenReturn(responseMap);
        })) {
            double[] coords = distanceService.getCoordinatesFromAddress("Địa chỉ không tồn tại");
            assertNull(coords);
        }
    }

    @Test
    void getCoordinatesFromAddress_throwsException_returnsNull() {
        try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class, (mock, context) -> {
            when(mock.getForObject(anyString(), eq(Map.class)))
                    .thenThrow(new RuntimeException("Parsing Error"));
        })) {
            double[] coords = distanceService.getCoordinatesFromAddress("Hà Nội");
            assertNull(coords); // Lỗi try-catch sẽ fallback về null[cite: 8]
        }
    }
}