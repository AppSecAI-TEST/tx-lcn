package com.lorne.tx.bean;

import com.lorne.tx.annotation.TxTransaction;
import com.lorne.tx.compensate.model.TransactionInvocation;

import java.lang.reflect.Method;

/**
 * 切面控制对象
 * Created by lorne on 2017/6/8.
 */
public class TxTransactionInfo {


    private TxTransaction transaction;

    private TxTransactionLocal txTransactionLocal;

    private String txGroupId;

    private int maxTimeOut;

    private TransactionLocal transactionLocal;


    private TxTransactionCompensate compensate;


    private TransactionInvocation invocation;


    public TxTransactionInfo(TxTransaction transaction, TxTransactionLocal txTransactionLocal, String txGroupId, int maxTimeOut, TransactionLocal transactionLocal, TxTransactionCompensate compensate, TransactionInvocation invocation) {
        this.transaction = transaction;
        this.txTransactionLocal = txTransactionLocal;
        this.txGroupId = txGroupId;
        this.maxTimeOut = maxTimeOut;
        this.transactionLocal = transactionLocal;
        this.compensate = compensate;
        this.invocation = invocation;
    }

    public int getMaxTimeOut() {
        return maxTimeOut;
    }

    public TransactionLocal getTransactionLocal() {
        return transactionLocal;
    }

    public TxTransaction getTransaction() {
        return transaction;
    }

    public TxTransactionLocal getTxTransactionLocal() {
        return txTransactionLocal;
    }

    public String getTxGroupId() {
        return txGroupId;
    }

    public TxTransactionCompensate getCompensate() {
        return compensate;
    }

    public TransactionInvocation getInvocation() {
        return invocation;
    }
}
