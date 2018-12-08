package com.tsw.blockchain.common.service;

import com.tsw.blockchain.common.entity.User;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

    private Map<Pair<User, User>, Double> secondDegreeTrustMap = new ConcurrentHashMap<>();

    private Map<User, Double> firstDegreeTrustMap = new ConcurrentHashMap<>();

    private Map<User, Double> reputationMap = new ConcurrentHashMap<>();

    public void calculateReputation(User target) {
        final List<User> neighborUsers = target.getNeighborUsers();

        //probably have a schedule job to refresh C for every node

        Double base = neighborUsers.stream().mapToDouble(
                neighbor -> firstDegreeTrustMap.get(target) * txHistoryService.calculateTxAmountTotal(Pair.of(neighbor, target))
        ).sum();


        Double reputation = neighborUsers.stream().mapToDouble(neighbor -> (
                localReputationService.getLocalReputationCurrentMap().get(Pair.of(neighbor, target)) * (
                        firstDegreeTrustMap.get(neighbor) * txHistoryService.calculateTxAmountTotal(Pair.of(neighbor, target)) / base
                ))).sum();

        reputationMap.put(target, reputation);
    }

    public Double calculateFirstDegreeTrustMap(User target, long timestamp) {
        //to be finalized: what is the range of P
        final List<User> neighborUsers = target.getNeighborUsers();
        Double c = 0d;
        if (neighborUsers.size() == 0) {
            c = 0d;
        } else {
            double phi = 1 / Math.exp(-1 / neighborUsers.size());
            c = neighborUsers
                    .stream()
                    .mapToDouble(neighborUser -> calculateSecondDegreeTrustMap(neighborUser, target, timestamp) * phi)
                    .average()
                    .orElse(0d);
        }
        firstDegreeTrustMap.put(target, c);
        return c;
    }

    //this is C_i,j
    public Double calculateSecondDegreeTrustMap(User initiator, User target, long timestamp) {
        final List<User> neighborUsers = target.getNeighborUsers();
        Double c = 0d;

        c = neighborUsers.stream()
                .mapToDouble(neighborUser -> calculateIndividualCredibility(initiator, neighborUser, target, timestamp))
                .filter(result -> result != 0.5)
                .average()
                .orElse(0);

        if (c == 0) {
            c = 0.5;
        }
        secondDegreeTrustMap.put(Pair.of(initiator, target), c);
        return c;
    }

    //this is C_i,j,jl (n)
    private Double calculateIndividualCredibility(User initiator, User neighbor, User target, long timestamp) {
        List<Object[]> txRatingList = txHistoryService.getTxRating(Pair.of(initiator, neighbor));
        Double inidividualCredibility = 0.5;
        if (!CollectionUtils.isEmpty(txRatingList)) {
            localReputationService.refreshLocalReputation(Pair.of(target, neighbor), timestamp);
//            THIS IS A PROBLEMATIC PART
//            localReputationService.refreshLocalReputation(Pair.of(initiator, neighbor), timestamp);
            List<Double> localReputationIndividualList = localReputationService
                    .getLocalReputationIndividualMap()
                    .get(Pair.of(target, neighbor));
            List<Double> smallSList = localReputationService.calculateSmallS(Pair.of(initiator, neighbor));
            inidividualCredibility = IntStream.range(0, txRatingList.size())
                    .mapToDouble(i -> (
                            smallSList.get(i) * (1 - Math.abs((Double) txRatingList.get(i)[0] - localReputationIndividualList.get(i + 1)))))
                    .sum();
        }
        return inidividualCredibility;
    }


    public Double calculateAdjustment(List<User> neighborUsers, User target, User initiator) {
        return neighborUsers.stream()
                .mapToDouble(neighbor ->
                        localReputationService.getBigMacMap().get(Pair.of(neighbor, target)))
                .average()
                .orElse(1);
    }
}
