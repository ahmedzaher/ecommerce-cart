package com.exequt.ecommerce.infrastructure.persistence;

import com.exequt.ecommerce.domain.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    Product toDomain(ProductEntity entity);

    ProductEntity toEntity(Product domain);
}
