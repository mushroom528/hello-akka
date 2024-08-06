package example.helloakka.actor.distributeddata;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CounterCache {

    public int cachedValue;

    public void increment(int value) {
        this.cachedValue += value;
    }
}
