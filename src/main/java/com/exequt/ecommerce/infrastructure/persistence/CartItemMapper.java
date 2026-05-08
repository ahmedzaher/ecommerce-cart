package com.exequt.ecommerce.infrastructure.persistence;

import com.exequt.ecommerce.domain.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartItemMapper {

    CartItem toDomain(CartItemEntity entity);

    CartItemEntity toEntity(CartItem domain);
}
