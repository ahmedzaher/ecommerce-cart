package com.exequt.ecommerce.infrastructure.persistence;

import com.exequt.ecommerce.application.dto.CartResponse;
import com.exequt.ecommerce.domain.model.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    Cart toDomain(CartEntity entity);

    CartEntity toEntity(Cart domain);

    CartResponse toResponse(Cart domain);
}
