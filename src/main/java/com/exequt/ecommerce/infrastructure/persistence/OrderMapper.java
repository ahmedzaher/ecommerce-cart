package com.exequt.ecommerce.infrastructure.persistence;

import com.exequt.ecommerce.application.dto.OrderResponse;
import com.exequt.ecommerce.domain.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    Order toDomain(OrderEntity entity);

    OrderEntity toEntity(Order domain);

    OrderResponse toResponse(Order domain);
}
