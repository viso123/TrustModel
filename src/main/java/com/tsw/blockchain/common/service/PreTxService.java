package com.tsw.blockchain.common.service;

import com.tsw.blockchain.common.entity.Transaction;
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
        updateLocalReputationAfterDecay(neighborUsers, target, initiator, timestamp);

    }

    private void updateLocalReputationAfterDecay(List<User> neighborUsers, User target, User initiator, long now) {
        //update local reputation after decay
        neighborUsers.forEach((neighbor) -> {
            Pair<User, User> userPair = Pair.of(initiator, neighbor);
            localReputationService.refreshLocalReputation(userPair, now);
            List<Transaction> txList = txHistoryService.getTxHistory(userPair);
            Double trustDegree = 0.5;
            if (!CollectionUtils.isEmpty(txList)) {
                List<Double> smallSList = localReputationService.getSmallSMap().get(userPair);
                List<Double> bigLList = localReputationService.getLocalReputationIndividualMap().get(userPair);

                trustDegree = IntStream.range(0, txList.size()).mapToDouble(i -> (
                        smallSList.get(i) * (1 - Math.abs(txList.get(i).getSellerRating() - bigLList.get(i + 1)))
                )).sum();
            }
            trustDegreeMap.put(userPair, trustDegree);
        });

        neighborUsers.forEach(neighbor -> {
            Pair<User, User> userPair = Pair.of(neighbor, target);
            localReputationService.refreshLocalReputation(userPair, now);
        });

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
