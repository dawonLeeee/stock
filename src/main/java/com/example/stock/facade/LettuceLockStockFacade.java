package com.example.stock.facade;

import com.example.stock.repository.RedisLockRepository;
import com.example.stock.service.StockService;
import org.springframework.stereotype.Component;

@Component
public class LettuceLockStockFacade {

    private RedisLockRepository redisLockRepository;
    private StockService stockService;

    public LettuceLockStockFacade(RedisLockRepository redisLockRepository, StockService stockService) {
        this.redisLockRepository = redisLockRepository;
        this.stockService = stockService;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while(!redisLockRepository.lock(id)) {
            // 100ms 후 lock획득 재시도 --> Lettuce에 걸린 부하를 줄여줄 수 있다
            Thread.sleep(100);
        }

        // lock획득에 성공했다면 stockService를 활용하여 재고감소 로직 진행
        try {
            stockService.decrease(id, quantity);
        } finally {
            // 로직이 모두 종료되었다면 unlock메서드를 활용하여 lock해제
            redisLockRepository.unlock(id);
        }
    }
}
