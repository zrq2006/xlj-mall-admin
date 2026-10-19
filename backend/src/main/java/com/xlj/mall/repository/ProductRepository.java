package com.xlj.mall.repository;

import com.xlj.mall.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    long countByCategoryId(Long categoryId);

    long countByBrandId(Long brandId);

    @Query("""
            select p from Product p
            where (:name is null or p.name like concat('%', :name, '%'))
              and (:categoryId is null or p.categoryId = :categoryId)
              and (:status is null or p.status = :status)
            order by p.id desc
            """)
    Page<Product> search(@Param("name") String name,
                         @Param("categoryId") Long categoryId,
                         @Param("status") Integer status,
                         Pageable pageable);
}
