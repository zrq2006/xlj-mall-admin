package com.xlj.mall.repository;

import com.xlj.mall.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOrderBySortAscIdAsc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query("""
            select c from Category c
            where (:name is null or c.name like concat('%', :name, '%'))
              and (:status is null or c.status = :status)
            order by c.sort asc, c.id asc
            """)
    Page<Category> search(@Param("name") String name, @Param("status") Integer status, Pageable pageable);
}
