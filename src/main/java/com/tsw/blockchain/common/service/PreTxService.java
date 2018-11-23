package com.tsw.blockchain.common.service;

import com.tsw.blockchain.common.entity.User;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class PreTxService {

    @Autowired
    private LocalReputationService localReputationService;

    @Autowired
    private TxRatingService txRatingService;

    @Autowired
    private TxHistoryService txHistoryService;

    private static final long effectiveTime = TimeUnit.DAYS.toMillis(30);

    public void calPreTxTrust(User initiator, User target) {
        Map<User, Double> creditsFromNeighbors = new HashMap<>();
        final List<User> neighborUsers = target.getNeighborUsers();
        long timestamp = new Date().getTime();
        updateLocalReputationAfterDecay(neighborUsers, target, timestamp);


    }

    private void updateLocalReputationAfterDecay(List<User> neighborUsers, User target, long now) {
        //update local reputation after decay
        neighborUsers.forEach((neighbor) -> {
            double valueAfterDecay;
            List<Pair<Long, Double>> evaluationHistoryList = neighbor.getLocalReputation().get(target);
            int listSize = evaluationHistoryList.size();
            Pair<Long, Double> lastEvaluation = evaluationHistoryList.get(listSize - 1);

            if ((now - lastEvaluation.getKey()) <= effectiveTime) {
                //to-do

                valueAfterDecay = 0.0;
            } else {
                //to-do
                valueAfterDecay = 0.0;
            }
            neighbor.getLocalReputation().get(target).add(Pair.of(now, valueAfterDecay));
        });
    }

    private double getTimeDecayFactor(List<Pair<Long, Double>> evaluationHistoryList, int index, long now) {

        long t0 = evaluationHistoryList.get(0).getLeft();
        return (now - t0) / ((now - t0)
                + evaluationHistoryList
                .stream()
                .mapToLong(i -> (i.getKey() - t0))
                .sum()
        );
    }


}
