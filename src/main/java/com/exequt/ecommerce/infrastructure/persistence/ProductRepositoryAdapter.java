package com.exequt.ecommerce.infrastructure.persistence;

import com.exequt.ecommerce.domain.model.Product;
import com.exequt.ecommerce.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpaRepository;
    private final ProductMapper productMapper;

    @Override
    public Optional<Product> findById(String id) {
        return jpaRepository.findById(id).map(productMapper::toDomain);
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = productMapper.toEntity(product);
        ProductEntity existing = jpaRepository.findById(product.getId()).orElse(null);
        if (existing != null) {
            entity.setVersion(existing.getVersion());
        }
        entity = jpaRepository.save(entity);
        return productMapper.toDomain(entity);
    }
}
