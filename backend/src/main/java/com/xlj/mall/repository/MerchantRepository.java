package com.xlj.mall.repository;

import com.xlj.mall.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    long countByAuditStatus(Integer auditStatus);
}
