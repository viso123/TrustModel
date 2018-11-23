package com.tsw.blockchain.common.service;

import com.tsw.blockchain.common.entity.User;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LocalReputationService {

    private Map<Pair<User, User>, List<Double>> localReputationIndividual;

    public LocalReputationService() {
        localReputationIndividual = new ConcurrentHashMap<>();
    }

    public Map<Pair<User, User>, List<Double>> getLocalReputationIndividual() {
        return localReputationIndividual;
    }

    public boolean refreshLocalReputation(User target, User neighbor, long timestamp) {
        //refreshed local reputation with respect to each neighbor node
        return true;
    }

}
