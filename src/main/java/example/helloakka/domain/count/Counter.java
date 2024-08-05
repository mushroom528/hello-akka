package example.helloakka.domain.count;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Counter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int numberValue;

    public static Counter of(String name) {
        Counter counter = new Counter();
        counter.name = name;
        return counter;
    }

    public void plus(int value) {
        this.numberValue += value;
    }
}
