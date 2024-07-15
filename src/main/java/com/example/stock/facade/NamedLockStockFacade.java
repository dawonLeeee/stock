package com.example.stock.facade;

import com.example.stock.repository.LockRepository;
import com.example.stock.service.StockService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NamedLockStockFacade {

    private final LockRepository lockRepository;

    private final StockService stockService;

    public NamedLockStockFacade(LockRepository lockRepository, StockService stockService) {
        this.lockRepository = lockRepository;
        this.stockService = stockService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    // 부모의 transaction과 별도로 실행되어야되기 때문에 이렇게 함
    /*
    * 현재 트랜잭션이 존재하더라도, 항상 새로운 트랜잭션을 시작합니다.
    * 기존 트랜잭션이 일시 중단되고 새로운 트랜잭션이 생성됩니다.
    * 새로운 트랜잭션이 완료되면 중단되었던 트랜잭션이 재개됩니다.*/
    public void decrease(Long id, Long quentity) {
        try {
            lockRepository.getLock(id.toString());
            stockService.decrease(id, quentity);
        } finally {
            // 모든 로직이 종료되었을때 lock 해제
            lockRepository.releaseLock(id.toString());
        }
    }
}
