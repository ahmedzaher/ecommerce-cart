package com.exequt.ecommerce.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class ProductEntity {

    @Id
    private String id;

    private String name;

    private BigDecimal price;

    private boolean available;

    @Version
    private Long version;
}
