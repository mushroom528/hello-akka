package example.helloakka.service;

import example.helloakka.domain.count.Counter;
import example.helloakka.domain.count.CounterRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CounterServiceTest {

    @Autowired
    private CounterService counterService;
    @Autowired
    private CounterRepository counterRepository;
    private Counter counter;

    @BeforeEach
    void setup() {
        counter = Counter.of("test");
        counterRepository.saveAndFlush(counter);
    }

    @AfterEach
    void clear() {
        counterRepository.delete(counter);
    }

    @Test
    void 동시에_올리기() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                counterService.plus(counter.getId(), 1);
            });
        }
        TimeUnit.SECONDS.sleep(1);

        Optional<Counter> result = counterRepository.findById(counter.getId());

        assertTrue(result.isPresent());
        assertEquals(100, result.get().getNumberValue());
    }

}