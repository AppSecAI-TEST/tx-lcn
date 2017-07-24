package com.lorne.tx.mq.service.impl;

import com.lorne.tx.manager.service.TxManagerService;
import com.lorne.tx.model.NotifyMsg;
import com.lorne.tx.mq.model.TxGroup;
import com.lorne.tx.mq.service.MQTxManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lorne on 2017/6/7.
 */
@Service
public class MQTxManagerServiceImpl implements MQTxManagerService {


    @Autowired
    private TxManagerService txManagerService;


    @Override
    public TxGroup createTransactionGroup() {
        return txManagerService.createTransactionGroup();
    }

    @Override
    public TxGroup addTransactionGroup(String groupId, String taskId, String modelName) {
        return txManagerService.addTransactionGroup(groupId, taskId, modelName);
    }

    @Override
    public boolean closeTransactionGroup(String groupId) {
        return txManagerService.closeTransactionGroup(groupId);
    }


    @Override
    public NotifyMsg notifyTransactionInfo(String groupId, String kid, boolean state) {
        return txManagerService.notifyTransactionInfo(groupId, kid, state);
    }


    @Override
    public boolean checkTransactionGroup(String groupId, String taskId) {
        return txManagerService.checkTransactionGroup(groupId, taskId);
    }

    @Override
    public boolean checkTransactionGroupState(String groupId) {
        return txManagerService.checkTransactionGroupState(groupId);
    }

    @Override
    public void dealTxGroup(TxGroup txGroup, boolean hasOk) {
        txManagerService.dealTxGroup(txGroup, hasOk);
    }

    @Override
    public boolean getHasOvertime(TxGroup txGroup) {
        return txManagerService.getHasOvertime(txGroup);
    }
}
