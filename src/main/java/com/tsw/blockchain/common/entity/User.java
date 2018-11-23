package com.tsw.blockchain.common.entity;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class User {
    private String userName;
    private List<User> neighborUsers;

    //Store history of past evaluation, (Timestamp, ReputationValue)
    private Map<User, List<Pair<Long, Double>>> localReputation;

    public User(String userName) {
        this.userName = userName;
        neighborUsers = new ArrayList<>();
        localReputation = new ConcurrentHashMap<>();
    }

    public String getUserName() {
        return userName;
    }

    public List<User> getNeighborUsers() {
        return neighborUsers;
    }

    public Map<User, List<Pair<Long, Double>>> getLocalReputation() {
        return localReputation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return new org.apache.commons.lang3.builder.EqualsBuilder()
                .append(userName, user.userName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang3.builder.HashCodeBuilder(17, 37)
                .append(userName)
                .toHashCode();
    }
}
