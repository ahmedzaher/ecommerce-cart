package com.exequt.ecommerce.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
public class CartEntity {

    @Id
    private String id;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItemEntity> items = new ArrayList<>();

    private boolean locked;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Version
    private Long version;

    public void addItem(CartItemEntity item) {
        items.add(item);
        item.setCart(this);
    }

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(CartItemEntity::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
