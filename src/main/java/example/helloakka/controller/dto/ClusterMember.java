package example.helloakka.controller.dto;

public record ClusterMember(String address, String status, boolean leader) {

    public static ClusterMember of(String address, String status, String leaderAddress) {
        return new ClusterMember(address, status, isLeader(address, leaderAddress));
    }

    private static boolean isLeader(String address, String leaderAddress) {
        return address.equals(leaderAddress);
    }
}
