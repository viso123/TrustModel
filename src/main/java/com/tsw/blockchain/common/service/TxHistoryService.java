package com.tsw.blockchain.common.service;

import com.tsw.blockchain.common.entity.Transaction;
import com.tsw.blockchain.common.entity.User;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TxHistoryService {
    private Map<Pair<User, User>, List<Transaction>> txHistory = new ConcurrentHashMap<>();

    //For i,j pair, Double[] has 3 elements, i's rating on j, tx amount, tx timestamp
    private Map<Pair<User, User>, List<Object[]>> txRating = new ConcurrentHashMap<>();

    public Map<Pair<User, User>, List<Transaction>> getTxHistory() {
        return txHistory;
    }

    public List<Transaction> getTxHistory(Pair<User, User> userPair) {
        return txHistory.getOrDefault(userPair, new ArrayList<>());
    }

    public Map<Pair<User, User>, List<Object[]>> getTxRating() {
        return txRating;
    }

    public List<Object[]> getTxRating(Pair<User, User> userPair) {
        return txRating.getOrDefault(userPair, new ArrayList<>());
    }

    public void add(Transaction tx) {
        txHistory.computeIfAbsent(Pair.of(tx.getBuyer(), tx.getSeller()), value -> new ArrayList<>())
                .add(tx);
        Object infoArray1[] = { tx.getSellerRating(), tx.getAmount(), tx.getTxTimestamp() };
        txRating.computeIfAbsent(Pair.of(tx.getBuyer(), tx.getSeller()), value -> new ArrayList<>())
                .add(infoArray1);
        Object infoArray2[] = { tx.getBuyerRating(), tx.getAmount(), tx.getTxTimestamp() };
        txRating.computeIfAbsent(Pair.of(tx.getSeller(), tx.getBuyer()), value -> new ArrayList<>())
                .add(infoArray2);
    }

    public Optional<Transaction> getLast(Pair<User, User> buyerSellerPair) {
        List<Transaction> list = txHistory.get(buyerSellerPair);
        if (list == null || list.size() == 0) {
            return Optional.empty();
        } else {
            return Optional.of(list.get(list.size() - 1));
        }
    }

}
