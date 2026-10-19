package com.xlj.mall.service;

import com.xlj.mall.dto.DashboardStats;
import com.xlj.mall.repository.MallOrderRepository;
import com.xlj.mall.repository.MemberRepository;
import com.xlj.mall.repository.MerchantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DashboardService {

    private final MallOrderRepository mallOrderRepository;
    private final MemberRepository memberRepository;
    private final MerchantRepository merchantRepository;

    public DashboardService(MallOrderRepository mallOrderRepository,
                            MemberRepository memberRepository,
                            MerchantRepository merchantRepository) {
        this.mallOrderRepository = mallOrderRepository;
        this.memberRepository = memberRepository;
        this.merchantRepository = merchantRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStats stats() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        BigDecimal todayPayAmount = mallOrderRepository.sumPaidAmountBetween(start, end);
        return new DashboardStats(
                mallOrderRepository.countByCreatedAtBetween(start, end),
                todayPayAmount == null ? BigDecimal.ZERO : todayPayAmount,
                mallOrderRepository.countByStatus("PENDING_PAY"),
                mallOrderRepository.countByStatus("PENDING_SHIP"),
                mallOrderRepository.count(),
                memberRepository.count(),
                merchantRepository.countByAuditStatus(0)
        );
    }
}
