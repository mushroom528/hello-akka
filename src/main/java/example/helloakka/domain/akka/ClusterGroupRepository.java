package example.helloakka.domain.akka;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClusterGroupRepository extends JpaRepository<ClusterGroup, Long> {

    void deleteByNodeName(String nodeName);
}