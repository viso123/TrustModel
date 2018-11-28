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

    //    effectively 1-based index
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
    public List<Double> refreshLocalReputation(Pair<User, User> userPair, long timestamp) {
        List<Transaction> txList = txHistoryService.getTxHistory(userPair);
        List<Double> localReputationForTxList = new ArrayList<>();
        localReputationForTxList.add(0.5);

        bigMacMap.put(userPair, DEFAULT_BIG_MAC);
        if (!CollectionUtils.isEmpty(txList)) {
            computeEffectiveLocalReputation(userPair, localReputationForTxList, txList);
        }

        computeCurrentLocalReputation(userPair, localReputationForTxList, txList, timestamp);
        return getLocalReputationIndividualMap().put(userPair, localReputationForTxList);
    }

    private Double computeCurrentLocalReputation(Pair<User, User> userPair,
                                                 List<Double> localReputationForTxList, List<Transaction> txList,
                                                 long timestamp) {
        Double latestLocalReputation = localReputationForTxList.get(localReputationForTxList.size() - 1);
        Double bigMac = bigMacMap.get(userPair);
        Double phiBigMac = Math.exp(-1 / bigMac);
        Double currenLocalReputation = 0d;
        if (!CollectionUtils.isEmpty(txList)) {
            if (timestamp - txList.get(txList.size() - 1).getTxTimestamp() <= EFFECTIVE_TIME_LOCAL_REPUTATION) {
                currenLocalReputation = latestLocalReputation * phiBigMac;
            } else {
                currenLocalReputation = (timestamp - T0) * 1d / (
                        (timestamp - T0) + txList.stream().mapToLong(tx -> (tx.getTxTimestamp() - T0)).sum()
                );
            }
        }
        return currenLocalReputation;
    }

    private void computeEffectiveLocalReputation(
            Pair<User, User> userPair, List<Double> localReputationForTxList, List<Transaction> txList) {
        double result = 0;
        List<Double> ruoList = new ArrayList<>();
        calculateRuo(userPair, txList, ruoList);
        for (int k = 0; k < txList.size(); k++) {
            result += txList.get(k).getSellerRating() * ruoList.get(k);
            localReputationForTxList.add(result);
        }
    }

    private void calculateRuo(Pair<User, User> userPair, List<Transaction> txList, List<Double> ruoList) {
        List<Double> s = new ArrayList<>();

        //        calculate s_k,l
        Long sumTime = txList.stream().mapToLong(i -> (i.getTxTimestamp() - T0)).sum();
        for (int i = 0; i < txList.size(); i++) {
            s.add(txList.get(i).getTxTimestamp() * 1.0d / sumTime);
        }
        smallSMap.put(userPair, s);

        Double M = IntStream.range(0, txList.size())
                .mapToDouble(i -> s.get(i) * txList.get(i).getAmount())
                .sum();
        bigMacMap.put(userPair, M);

        ruoList = (List<Double>) IntStream.range(0, txList.size())
                .mapToDouble(i -> s.get(i) * txList.get(i).getAmount() / M)
                .boxed()
                .collect(Collectors.toList());
    }
}
