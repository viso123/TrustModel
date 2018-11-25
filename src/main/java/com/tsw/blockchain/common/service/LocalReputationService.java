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

    public LocalReputationService() {
        localReputationIndividualMap = new ConcurrentHashMap<>();
        localReputationCurrentMap = new ConcurrentHashMap<>();
        bigMacMap = new ConcurrentHashMap<>();
    }

    public Map<Pair<User, User>, List<Double>> getLocalReputationIndividualMap() {
        return localReputationIndividualMap;
    }

    public Map<Pair<User, User>, Double> getLocalReputationCurrentMap() {
        return localReputationCurrentMap;
    }

    //    Calculate local reputation L_i,j according to formula
    public List<Double> refreshLocalReputation(User target, User neighbor, long timestamp) {
        double bigMac;
        List<Transaction> txList = txHistoryService.getTxHistory(neighbor, target);
        List<Double> localReputationForTxList = new ArrayList<>();
        localReputationForTxList.add(0.5);
        if (!CollectionUtils.isEmpty(txList)) {
            bigMac = computeEffectiveLocalReputation(localReputationForTxList, txList);
        } else {
            bigMac = DEFAULT_BIG_MAC;
        }
        bigMacMap.put(Pair.of(neighbor, target), bigMac);

        computeCurrentLocalReputation(localReputationForTxList, txList, timestamp, bigMac);
        return getLocalReputationIndividualMap().put(Pair.of(neighbor, target), localReputationForTxList);
    }

    private Double computeCurrentLocalReputation(List<Double> localReputationForTxList, List<Transaction> txList,
                                                 long timestamp, double bigMac) {
        Double latestLocalReputation = localReputationForTxList.get(localReputationForTxList.size() - 1);
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

    private Double computeEffectiveLocalReputation(List<Double> localReputationForTxList, List<Transaction> txList) {
        double result = 0;
        List<Double> ruoList = new ArrayList<>();
        Double bigMac = calculateRuo(txList, ruoList);
        for (int k = 1; k <= txList.size(); k++) {
            result += txList.get(k - 1).getSellerRating() * ruoList.get(k - 1);
            localReputationForTxList.add(result);
        }
        return bigMac;
    }

    private Double calculateRuo(List<Transaction> txList, List<Double> ruoList) {
        List<Double> s = new ArrayList<>();

        //        calculate s_k,l
        Long sumTime = txList.stream().mapToLong(i -> (i.getTxTimestamp() - T0)).sum();
        for (int i = 0; i < txList.size(); i++) {
            s.add(txList.get(i).getTxTimestamp() * 1.0d / sumTime);
        }

        Double M = IntStream.range(0, txList.size())
                .mapToDouble(i -> s.get(i) * txList.get(i).getAmount())
                .sum();


        ruoList = (List<Double>) IntStream.range(0, txList.size())
                .mapToDouble(i -> s.get(i) * txList.get(i).getAmount() / M)
                .boxed()
                .collect(Collectors.toList());

        return M;
    }
}
