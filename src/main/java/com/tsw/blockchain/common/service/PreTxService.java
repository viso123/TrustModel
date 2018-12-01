package com.tsw.blockchain.common.service;

import com.tsw.blockchain.common.entity.User;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Service
public class PreTxService {

    @Autowired
    private LocalReputationService localReputationService;

    @Autowired
    private TxHistoryService txHistoryService;

    private Map<Pair<User, User>, Double> trustDegreeMap = new ConcurrentHashMap<>();

    public void calPreTxTrust(User initiator, User target) {
        Map<User, Double> creditsFromNeighbors = new HashMap<>();
        final List<User> neighborUsers = target.getNeighborUsers();
        final long timestamp = new Date().getTime();

    }

    //this is C_i,j,jl (n)
    private Double calculateIndividualCredibility(User initiator, User neighbor, User target, long timestamp) {
        List<Object[]> txRatingList = txHistoryService.getTxRating(Pair.of(initiator, neighbor));
        Double inidividualCredibility = 0.5;
        if (!CollectionUtils.isEmpty(txRatingList)) {
            localReputationService.refreshLocalReputation(Pair.of(neighbor, target), timestamp);
            List<Double> localReputationIndividualList = localReputationService
                    .getLocalReputationIndividualMap()
                    .get(Pair.of(neighbor, target));
            List<Double> smallSList = localReputationService.calculateSmallS(Pair.of(initiator, target));
            inidividualCredibility = IntStream.range(0, txRatingList.size())
                    .mapToDouble(i -> (
                            smallSList.get(i) * (1 - Math.abs((Double) txRatingList.get(i)[0] - localReputationIndividualList.get(i + 1)))))
                    .sum();
        }
        return inidividualCredibility;
    }

    public Double calculateTrustDegree(List<User> neighborUsers, User target, User initiator) {
        return neighborUsers.stream()
                .mapToDouble(neighbor -> trustDegreeMap.get(Pair.of(neighbor, target)))
                .average()
                .orElse(0);
    }

    public Double calculateAdjustment(List<User> neighborUsers, User target, User initiator) {
        return neighborUsers.stream()
                .mapToDouble(neighbor ->
                        localReputationService.getBigMacMap().get(Pair.of(neighbor, target)))
                .average()
                .orElse(1);
    }
}
