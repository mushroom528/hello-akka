package example.helloakka.service;

import example.helloakka.domain.count.CounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CounterService {

    private final CounterRepository counterRepository;

    @Transactional
    public void plus(Long id, int value) {
        counterRepository.findById(id).ifPresent(counter -> counter.plus(value));
    }
}
