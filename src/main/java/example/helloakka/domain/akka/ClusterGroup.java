package example.helloakka.domain.akka;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class ClusterGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nodeName;

    public static ClusterGroup of(String nodeName) {
        ClusterGroup clusterGroup = new ClusterGroup();
        clusterGroup.nodeName = nodeName;
        return clusterGroup;
    }

}
