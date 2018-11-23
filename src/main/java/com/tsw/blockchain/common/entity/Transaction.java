package com.tsw.blockchain.common.entity;

import org.apache.commons.lang3.tuple.Pair;

public class Transaction {
    private User buyer;
    private User seller;
    private Product product;
    private double amount;
    private long txTimestamp;
    private long txId;

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getTxTimestamp() {
        return txTimestamp;
    }

    public void setTxTimestamp(long txTimestamp) {
        this.txTimestamp = txTimestamp;
    }

    public long getTxId() {
        return txId;
    }

    public void setTxId(long txId) {
        this.txId = txId;
    }
}
