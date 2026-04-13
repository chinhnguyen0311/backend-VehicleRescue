package com.Doantotnghiep.vehicle_rescue.rescue_management.service;

import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;
import com.Doantotnghiep.vehicle_rescue.authentication.repository.AccountRepository;
import com.Doantotnghiep.vehicle_rescue.authentication.util.SecurityUtil;
import com.Doantotnghiep.vehicle_rescue.common.exception.CustomException;
import com.Doantotnghiep.vehicle_rescue.common.exception.ErrorCode;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.AddMechanicServiceRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.UpdateMechanicServiceRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.UpdateProfileRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.MechanicOrderItemResponse;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.MechanicServiceResponseDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.ProfileResponseDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Mechanic;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.MechanicService;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.MechanicServiceId;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.RescueOrder;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicWorkType;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.OrderStatus;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.MechanicRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.MechanicServiceRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.RescueOrderRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.service.map.DistanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MechanicProfileService {
    private final AccountRepository accountRepository;
    private final MechanicRepository mechanicRepository;
    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);
    private final MechanicServiceRepository mechanicServiceRepository;
    private final DistanceService distanceService;
    private final RescueOrderRepository rescueOrderRepository;
    public ProfileResponseDTO getProfile() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        ProfileResponseDTO.ProfileResponseDTOBuilder builder = ProfileResponseDTO.builder()
                .fullName(account.getFullName())
                .email(account.getEmail())
                .phoneNumber(account.getPhoneNumber())
                .type(mechanic.getType())
                .workType(mechanic.getWorkType())
                .subsEndDate(mechanic.getSubsEndDate());

        if (mechanic.getWorkType() == MechanicWorkType.GARAGE) {
            builder
                    .garageName(mechanic.getGarageName())
                    .garageAddress(mechanic.getGarageAddress());
        }
        return builder.build();
    }
    public ProfileResponseDTO updateProfile(UpdateProfileRequestDTO request){
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        // Cập nhật Account
        account.setFullName(request.getFullName());
        account.setPhoneNumber(request.getPhoneNumber());
        accountRepository.save(account);

        // Cập nhật Mechanic
        mechanic.setType(request.getType());
        mechanic.setDisplayName(request.getFullName());
        mechanic.setPhoneNumber(request.getPhoneNumber());

        // Nếu là GARAGE thì validate và cập nhật thông tin garage
        if (mechanic.getWorkType() == MechanicWorkType.GARAGE) {
            mechanic.setGarageName(request.getGarageName());
            mechanic.setGarageAddress(request.getGarageAddress());

            if (request.getGarageLatitude() != null && request.getGarageLongitude() != null) {
                Point garageLocation = GEOMETRY_FACTORY.createPoint(
                        new Coordinate(request.getGarageLongitude(), request.getGarageLatitude())
                );
                mechanic.setGarageLocation(garageLocation);
            }
        }

        mechanicRepository.save(mechanic);
        return getProfile();
    }
    public void addService(AddMechanicServiceRequestDTO request) {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (mechanicServiceRepository.existsByMechanicIdAndServiceId(
                mechanic.getMechanicId(), request.getServiceId())) {
            throw new CustomException(ErrorCode.SERVICE_ALREADY_EXISTS);
        }

        MechanicService mechanicService = MechanicService.builder()
                .mechanicId(mechanic.getMechanicId())
                .serviceId(request.getServiceId())
                .customPrice(request.getCustomPrice())
                .build();

        mechanicServiceRepository.save(mechanicService);
    }
    public List<MechanicServiceResponseDTO> getServices() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        return mechanicServiceRepository.findServicesByMechanicId(mechanic.getMechanicId());
    }
    public void updateServicePrice(UUID serviceId, UpdateMechanicServiceRequestDTO request) {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        MechanicService mechanicService = mechanicServiceRepository
                .findById(new MechanicServiceId(mechanic.getMechanicId(), serviceId))
                .orElseThrow(() -> new CustomException(ErrorCode.SERVICE_NOT_FOUND));

        mechanicService.setCustomPrice(request.getCustomPrice());
        mechanicServiceRepository.save(mechanicService);
    }
    public List<MechanicOrderItemResponse> getRequestedOrdersForMechanic() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Double mechanicLat = extractLat(
                mechanic.getWorkType().name().equals("GARAGE")
                        ? mechanic.getGarageLocation()
                        : mechanic.getCurrentLocation()
        );

        Double mechanicLng = extractLng(
                mechanic.getWorkType().name().equals("GARAGE")
                        ? mechanic.getGarageLocation()
                        : mechanic.getCurrentLocation()
        );

        List<RescueOrder> orders = rescueOrderRepository
                .findByMechanicIdAndStatus(mechanic.getMechanicId(), OrderStatus.REQUESTED);

        return orders.stream().map(order -> {

            Double cusLat = extractLat(order.getCustomerLocation());
            Double cusLng = extractLng(order.getCustomerLocation());

            Double distance = distanceService.getDistance(
                    mechanicLat, mechanicLng,
                    cusLat, cusLng
            );

            String address = distanceService.getAddress(cusLat, cusLng);

            return MechanicOrderItemResponse.builder()
                    .orderId(order.getOrderId())
                    .customerName(order.getCustomerName())
                    .customerPhone(order.getCustomerPhone())
                    .latitude(cusLat)
                    .longitude(cusLng)
                    .distance(distance)
                    .address(address)
                    .build();

        }).toList();
    }
    public void acceptOrder(UUID orderId) {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        RescueOrder order = rescueOrderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // Kiểm tra đơn có phải của mechanic này không
        if (!order.getMechanicId().equals(mechanic.getMechanicId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // Chỉ accept khi đang ở trạng thái REQUESTED
        if (order.getStatus() != OrderStatus.REQUESTED) {
            throw new CustomException(ErrorCode.ORDER_INVALID_STATUS);
        }

        order.setStatus(OrderStatus.ACCEPTED);
        rescueOrderRepository.save(order);
    }

    public void cancelOrder(UUID orderId) {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        RescueOrder order = rescueOrderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMechanicId().equals(mechanic.getMechanicId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // Chỉ cancel khi đang REQUESTED hoặc ACCEPTED
        if (order.getStatus() != OrderStatus.REQUESTED
                && order.getStatus() != OrderStatus.ACCEPTED) {
            throw new CustomException(ErrorCode.ORDER_INVALID_STATUS);
        }

        order.setStatus(OrderStatus.CANCELLED);
        rescueOrderRepository.save(order);
    }
    public void inProcessOrder(UUID orderId) {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        RescueOrder order = rescueOrderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMechanicId().equals(mechanic.getMechanicId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (order.getStatus() != OrderStatus.ACCEPTED) {
            throw new CustomException(ErrorCode.ORDER_INVALID_STATUS);
        }

        order.setStatus(OrderStatus.IN_PROGRESS);
        rescueOrderRepository.save(order);
    }
    public void completeOrder(UUID orderId) {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Mechanic mechanic = mechanicRepository.findByAccount(account)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        RescueOrder order = rescueOrderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getMechanicId().equals(mechanic.getMechanicId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new CustomException(ErrorCode.ORDER_INVALID_STATUS);
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(OffsetDateTime.now());
        rescueOrderRepository.save(order);
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

}
