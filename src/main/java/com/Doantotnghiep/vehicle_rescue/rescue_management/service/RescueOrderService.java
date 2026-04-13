package com.Doantotnghiep.vehicle_rescue.rescue_management.service;

import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.CreateRescueOrderRequest;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.MechanicSearchResultDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.SearchMechanicRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.MechanicServiceResponseDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.RescueOrderResponse;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.RescueOrder;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.OrderStatus;
import com.Doantotnghiep.vehicle_rescue.rescue_management.mapper.RescueOrderMapper;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.MechanicRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.MechanicServiceRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.RescueOrderRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.service.map.DistanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RescueOrderService {
    private final MechanicServiceRepository mechanicServiceRepository;
    private final RescueOrderRepository rescueOrderRepository;
    private final MechanicRepository mechanicRepository;
    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);
    private final DistanceService distanceService;
    private final RescueOrderMapper rescueOrderMapper;
    public List<MechanicSearchResultDTO> searchNearbyMechanics(SearchMechanicRequestDTO request) {
        List<Object[]> results = mechanicRepository.findNearbyMechanics(
                request.getLatitude(),
                request.getLongitude(),
                request.getServiceId()
        );

        List<MechanicSearchResultDTO> list = results.stream().map(row -> {
            UUID mechanicId = UUID.fromString(row[0].toString());

            List<MechanicServiceResponseDTO> services =
                    mechanicServiceRepository.findServicesByMechanicId(mechanicId);

            int completedOrders = rescueOrderRepository
                    .countByMechanicIdAndStatus(mechanicId, OrderStatus.COMPLETED);

            String type = row[1].toString();
            List<String> types = List.of(type);

            String workType = row[7].toString();

            String displayName = workType.equals("GARAGE")
                    ? (String) row[5]
                    : (String) row[2];

            Double lat = workType.equals("GARAGE")
                    ? extractLat(row[6])
                    : extractLat(row[4]);

            Double lng = workType.equals("GARAGE")
                    ? extractLng(row[6])
                    : extractLng(row[4]);

            return MechanicSearchResultDTO.builder()
                    .mechanicId(mechanicId)
                    .displayName(displayName)
                    .latitude(lat)
                    .longitude(lng)
                    .phoneNumber((String) row[3])
                    .type(types)
                    .services(services)
                    .ratingScore(row[8] != null ? new BigDecimal(row[8].toString()) : BigDecimal.ZERO)
                    .completedOrders(completedOrders)
                    .distance((Double) row[row.length - 1]) // tạm thời là distance thẳng
                    .build();
        }).toList();
        List<MechanicSearchResultDTO> topList = new ArrayList<>(
                list.stream()
                        .filter(m -> m.getLatitude() != null && m.getLongitude() != null)
                        .limit(5)
                        .toList()
        );

// 🔥 gọi ORS lấy distance thật
        List<Double> realDistances = distanceService.getRealDistances(
                request.getLatitude(),
                request.getLongitude(),
                topList
        );

// 🔥 gán lại distance
        for (int i = 0; i < topList.size(); i++) {
            topList.get(i).setDistance(realDistances.get(i));
        }

// (optional) sort lại theo distance thật
        topList.sort(Comparator.comparing(MechanicSearchResultDTO::getDistance));

        return topList;

    }

    private Double extractLat(Object pointObj) {
        if (pointObj == null) return null;

        if (pointObj instanceof org.locationtech.jts.geom.Point p) {
            return p.getY();
        }

        if (pointObj instanceof org.geolatte.geom.Point p) {
            return p.getPosition().getCoordinate(1); // lat
        }

        return null;
    }

    private Double extractLng(Object pointObj) {
        if (pointObj == null) return null;

        if (pointObj instanceof org.locationtech.jts.geom.Point p) {
            return p.getX();
        }

        if (pointObj instanceof org.geolatte.geom.Point p) {
            return p.getPosition().getCoordinate(0); // lng
        }

        return null;
    }

    public RescueOrderResponse createRescueOrder(CreateRescueOrderRequest request) {

        org.locationtech.jts.geom.Point point = GEOMETRY_FACTORY.createPoint(
                new org.locationtech.jts.geom.Coordinate(
                        request.getLongitude(),
                        request.getLatitude()
                )
        );

        var mechanic = mechanicRepository.findById(request.getMechanicId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thợ"));

        String mechanicName = mechanic.getWorkType().name().equals("GARAGE")
                ? mechanic.getGarageName()
                : mechanic.getDisplayName();

        RescueOrder order = RescueOrder.builder()
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerLocation(point)
                .serviceId(request.getServiceId())
                .mechanicId(mechanic.getMechanicId())
                .mechanicName(mechanicName)
                .status(OrderStatus.REQUESTED)
                .build();

        RescueOrder saved = rescueOrderRepository.save(order);

        // 🔥 convert sang response bằng mapper
        return rescueOrderMapper.toResponse(saved);
    }
}
