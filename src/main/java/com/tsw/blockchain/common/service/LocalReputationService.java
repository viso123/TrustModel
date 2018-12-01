package com.tsw.blockchain.common.service;

import com.tsw.blockchain.common.entity.Transaction;
import com.tsw.blockchain.common.entity.User;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.tsw.blockchain.common.Constants.*;

@Service
public class LocalReputationService {

    @Autowired
    private TxHistoryService txHistoryService;

    //    effectively 1-based index, [0] = 0.5 --> DEFAULT VALUE
    private Map<Pair<User, User>, List<Double>> localReputationIndividualMap;

    private Map<Pair<User, User>, Double> localReputationCurrentMap;

    private Map<Pair<User, User>, Double> bigMacMap;

    private Map<Pair<User, User>, List<Double>> smallSMap;


    public LocalReputationService() {
        localReputationIndividualMap = new ConcurrentHashMap<>();
        localReputationCurrentMap = new ConcurrentHashMap<>();
        bigMacMap = new ConcurrentHashMap<>();
        smallSMap = new ConcurrentHashMap<>();
    }

    public Map<Pair<User, User>, List<Double>> getLocalReputationIndividualMap() {
        return localReputationIndividualMap;
    }

    public Map<Pair<User, User>, Double> getLocalReputationCurrentMap() {
        return localReputationCurrentMap;
    }

    public Map<Pair<User, User>, Double> getBigMacMap() {
        return bigMacMap;
    }

    public Map<Pair<User, User>, List<Double>> getSmallSMap() {
        return smallSMap;
    }

    //    Calculate local reputation L_i,j according to formula
    public void refreshLocalReputation(Pair<User, User> userPair, long timestamp) {
        List<Transaction> txList = txHistoryService.getTxHistory(userPair);
        List<Double> localReputationForTxList = new ArrayList<>();

        //L_i,j (n)
        localReputationForTxList.add(0.5);
        computeEffectiveLocalReputation(userPair, localReputationForTxList);

        //L_i,j derived from L_i,j (n)
        computeCurrentLocalReputation(userPair, localReputationForTxList.get(localReputationForTxList.size() - 1), timestamp);
    }


    public Double computeCurrentLocalReputation(Pair<User, User> userPair,
                                                Double latestLocalReputation,
                                                long timestamp) {
        List<Object[]> txRatingList = txHistoryService.getTxRating(userPair);
        Double bigMac = bigMacMap.get(userPair);
        Double phiBigMac = Math.exp(-1 / bigMac);
        Double currenLocalReputation = 0d;
        if (!CollectionUtils.isEmpty(txRatingList)) {
            if (timestamp - (Long) txRatingList.get(txRatingList.size() - 1)[2] <= EFFECTIVE_TIME_LOCAL_REPUTATION) {
                currenLocalReputation = latestLocalReputation * phiBigMac;
            } else {
                currenLocalReputation = (timestamp - T0) * 1d / (
                        (timestamp - T0) + txRatingList.stream().mapToLong(tx -> ((Long) tx[2] - T0)).sum()
                );
            }
        }
        localReputationCurrentMap.put(userPair, currenLocalReputation);
        return currenLocalReputation;
    }

    public void computeEffectiveLocalReputation(
            Pair<User, User> userPair, List<Double> localReputationForTxList) {
        double result = 0;
        List<Object[]> txRatingList = txHistoryService.getTxRating(userPair);


        List<Double> ruoList = calculateRuo(userPair);
        for (int k = 0; k < txRatingList.size(); k++) {
            result += (Double) txRatingList.get(k)[0] * ruoList.get(k);
            localReputationForTxList.add(result);
        }
        localReputationIndividualMap.put(userPair, localReputationForTxList);
    }

    public List<Double> calculateRuo(Pair<User, User> userPair) {
        List<Double> s = calculateSmallS(userPair);

        Double M = calculateBigMac(userPair);

        List<Object[]> txRatingList = txHistoryService.getTxRating(userPair);
        return (List<Double>) IntStream.range(0, txRatingList.size())
                .mapToDouble(i -> s.get(i) * (Double) txRatingList.get(i)[1] / M)
                .boxed()
                .collect(Collectors.toList());
    }

    public List<Double> calculateSmallS(Pair<User, User> userPair) {
        List<Double> s = new ArrayList<>();
        List<Object[]> txRatingList = txHistoryService.getTxRating(userPair);
        //        calculate s_k,l
        if (!CollectionUtils.isEmpty(txRatingList)) {
            Long sumTime = txRatingList.stream().mapToLong(i -> ((Long) i[2] - T0)).sum();
            for (int i = 0; i < txRatingList.size(); i++) {
                s.add((Long) txRatingList.get(i)[2] * 1.0d / sumTime);
            }
        }
        smallSMap.put(userPair, s);
        return s;
    }

    public Double calculateBigMac(Pair<User, User> userPair) {
        List<Double> s = smallSMap.get(userPair);
        List<Object[]> txRatingList = txHistoryService.getTxRating(userPair);
        Double M;
        if (CollectionUtils.isEmpty(txRatingList)) {
            M = DEFAULT_BIG_MAC;
        } else {
            M = IntStream.range(0, txRatingList.size())
                    .mapToDouble(i -> s.get(i) * (Double) txRatingList.get(i)[1])
                    .sum();
        }
        bigMacMap.put(userPair, M);
        return M;
    }

}
