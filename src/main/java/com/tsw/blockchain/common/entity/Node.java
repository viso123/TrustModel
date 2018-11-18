package com.tsw.blockchain.common.entity;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Node {
    private String userName;
    private List<Node> neighborNodes;

    //Store history of past evaluation, (Timestamp, ReputationValue)
    private Map<Node, List<Pair<Long, Double>>> localReputation;

    public Node(String userName) {
        this.userName = userName;
        neighborNodes = new ArrayList<>();
        localReputation = new ConcurrentHashMap<>();
    }

    public String getUserName() {
        return userName;
    }

    public List<Node> getNeighborNodes() {
        return neighborNodes;
    }

    public Map<Node, List<Pair<Long, Double>>> getLocalReputation() {
        return localReputation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return new org.apache.commons.lang3.builder.EqualsBuilder()
                .append(userName, node.userName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang3.builder.HashCodeBuilder(17, 37)
                .append(userName)
                .toHashCode();
    }
}
