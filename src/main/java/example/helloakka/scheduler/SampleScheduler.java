package example.helloakka.scheduler;

import example.helloakka.cluster.ClusterGroupManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SampleScheduler {

    private final ClusterGroupManager clusterGroupManager;

    @Scheduled(fixedDelay = 10000)
    public void sample() {
        boolean leader = clusterGroupManager.isLeader();
        if (!leader) return;
        log.info("Sample scheduled");
    }
}
