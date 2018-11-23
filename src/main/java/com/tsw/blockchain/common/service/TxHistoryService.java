package com.tsw.blockchain.common.service;

import com.tsw.blockchain.common.entity.User;
import com.tsw.blockchain.common.entity.Transaction;
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

    public Map<Pair<User, User>, List<Transaction>> getTxHistory() {
        return txHistory;
    }

    public boolean add(Transaction tx) {
        return txHistory
                .computeIfAbsent(tx.getBuyerSellerPair(), value -> new ArrayList<>())
                .add(tx);
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
