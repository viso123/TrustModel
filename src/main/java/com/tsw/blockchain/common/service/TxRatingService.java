package com.tsw.blockchain.common.service;

import com.tsw.blockchain.common.entity.Node;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TxRatingService {
    private Map<Pair<Node, Node>, Double> txRatingSum;

    private Map<Pair<Node, Node>, List<Double>> txRatingIndividual;

    public TxRatingService() {
        txRatingIndividual = new ConcurrentHashMap<>();
        txRatingSum = new ConcurrentHashMap<>();
    }

    public Map<Pair<Node, Node>, Double> getTxRatingSum() {
        return txRatingSum;
    }

    public Map<Pair<Node, Node>, List<Double>> getTxRatingIndividual() {
        return txRatingIndividual;
    }
}
