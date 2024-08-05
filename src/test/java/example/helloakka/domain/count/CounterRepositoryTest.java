package example.helloakka.domain.count;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CounterRepositoryTest {

    @Autowired
    private CounterRepository counterRepository;

    @Test
    void plus() {

        Counter test = Counter.of("test");
        test.plus(100);

        counterRepository.save(test);
        Optional<Counter> counter = counterRepository.findById(test.getId());

        assertTrue(counter.isPresent());
        assertEquals(100, counter.get().getNumberValue());
    }

}