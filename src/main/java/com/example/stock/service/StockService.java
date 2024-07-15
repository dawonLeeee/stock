package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

//    @Transactional
    public synchronized void decrease(Long id, Long quantity) {
        /* synchronized 키워드를 사용하여 1개의 스레드만 접근할 수 있도록 설정
        * BUT 테스트케이스 실해
        * 왜? @Transactional 때문인데, @Transactional은 기본적으로 메소드에 대해 프록시를 생성하고, 프록시는 메소드 호출을 감싸서 트랜잭션을 시작하고 종료함
        * 그래서 synchronized 키워드가 메소드에 적용되지 않음
        * 해결: @Transactional 어노테이션을 주석처리
        * */

        // Stock 조회
        // 재고 감소시킨뒤
        // 갱신된 값 저장

        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decreate(quantity);
        stockRepository.saveAndFlush(stock);
    }
}
