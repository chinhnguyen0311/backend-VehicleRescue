package com.Doantotnghiep.vehicle_rescue.rescue_management.mapper;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.RescueOrderResponse;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.RescueOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RescueOrderMapper {
    @Mapping(target = "latitude", expression = "java(order.getCustomerLocation() != null ? order.getCustomerLocation().getY() : null)")
    @Mapping(target = "longitude", expression = "java(order.getCustomerLocation() != null ? order.getCustomerLocation().getX() : null)")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "orderStatus", source = "status")
    RescueOrderResponse toResponse(RescueOrder order);
}
