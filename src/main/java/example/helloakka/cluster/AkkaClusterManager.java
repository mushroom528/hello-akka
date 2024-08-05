package example.helloakka.cluster;

import akka.cluster.Member;
import akka.cluster.typed.Cluster;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Slf4j
public class AkkaClusterManager {

    private final ClusterGroupManager clusterGroupManager;
    private final Cluster cluster;

    @PostConstruct
    void init() {
        String address = cluster.selfMember().address().toString();
        clusterGroupManager.join(address);
        log.info("클러스터 join: {}", address);
    }

    @PreDestroy
    void destroy() {
        String address = cluster.selfMember().address().toString();
        clusterGroupManager.leave(address);
        log.info("클러스터 leave: {}", address);
    }

    public List<Member> getNodes() {
        Iterable<Member> members = cluster.state().getMembers();
        return StreamSupport.stream(members.spliterator(), false).toList();
    }

}
