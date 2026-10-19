package com.xlj.mall.repository;

import com.xlj.mall.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    List<Brand> findAllByOrderBySortAscIdAsc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query("""
            select b from Brand b
            where (:name is null or b.name like concat('%', :name, '%'))
              and (:status is null or b.status = :status)
            order by b.sort asc, b.id asc
            """)
    Page<Brand> search(@Param("name") String name, @Param("status") Integer status, Pageable pageable);
}
