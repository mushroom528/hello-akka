package example.helloakka.cluster;

import akka.actor.Address;
import akka.actor.AddressFromURIString;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.JoinSeedNodes;
import example.helloakka.domain.akka.ClusterGroup;
import example.helloakka.domain.akka.ClusterGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
@Transactional
public class ClusterGroupManager {

    private final ClusterGroupRepository clusterGroupRepository;
    private final Cluster cluster;

    public void join(String nodeAddress) {
        clusterGroupRepository.save(ClusterGroup.of(nodeAddress));
        seedNodeUpdate();
    }

    public void leave(String nodeAddress) {
        clusterGroupRepository.deleteByNodeName(nodeAddress);
        seedNodeUpdate();
    }

    public boolean isLeader() {
        String self = cluster.selfMember().address().toString();
        Address leader = cluster.state().getLeader();
        if (leader == null) return false;

        return self.equals(leader.toString());
    }

    private void seedNodeUpdate() {
        cluster.manager().tell(new JoinSeedNodes(getClusterGroups()));
    }

    public List<Address> getClusterGroups() {
        return clusterGroupRepository.findAll().stream()
                .map(clusterGroup -> AddressFromURIString.parse(clusterGroup.getNodeName()))
                .toList();
    }
}
