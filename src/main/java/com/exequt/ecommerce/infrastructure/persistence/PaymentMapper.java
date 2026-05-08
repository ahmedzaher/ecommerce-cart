package com.exequt.ecommerce.infrastructure.persistence;

import com.exequt.ecommerce.application.dto.PaymentResponse;
import com.exequt.ecommerce.application.dto.PaymentStartResponse;
import com.exequt.ecommerce.domain.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(target = "paymentId", source = "id")
    PaymentStartResponse toStartResponse(Payment domain);

    Payment toDomain(PaymentEntity entity);

    PaymentEntity toEntity(Payment domain);

    PaymentResponse toResponse(Payment domain);
}
