package com.xlj.mall.repository;

import com.xlj.mall.entity.MallOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface MallOrderRepository extends JpaRepository<MallOrder, Long> {

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatus(String status);

    @Query("""
            select coalesce(sum(o.payAmount), 0) from MallOrder o
            where o.paidAt is not null and o.paidAt between :start and :end
            """)
    BigDecimal sumPaidAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
