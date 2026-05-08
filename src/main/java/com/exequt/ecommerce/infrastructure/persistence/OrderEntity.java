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
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity {

    @Id
    private String id;

    @Column(name = "cart_id")
    private String cartId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    private String state;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Version
    private Long version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.setOrder(this);
    }
}
