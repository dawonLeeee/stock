package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
class StockServiceTest {

    @Autowired
    private PessimisticLockStockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before() {
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    @Test
    public void 재고감소() {
        stockService.decrease(1L, 1L);

        // 100 -1 = 99
        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(99, stock.getQuantity());
    }

    @Test
    public void 동시에_여러개의_요청() throws InterruptedException {
        int threadCount = 100;
        // 새 스레드풀 생성(32개)-->왜? 일반적으로 CPU코어 수의 1배/2배 정도로 생성
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        // 하나 이상의 스레드가 다른 스레드들이 수행중인 작업이 완료될때까지 기다림
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);
                } finally {
                    // 각 스레드가 작업을 마치면 값을 1씩 감소시킴
                    latch.countDown();
                }

            });
        }

        // CountDownLatch의 값이 0이 될때까지 현재 스레드를 대기시킨다
        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();
        // 100 - (1*100) = 0
        assertEquals(0, stock.getQuantity());

    }

}