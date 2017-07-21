package com.lorne.tx.service.impl;

import com.lorne.core.framework.utils.KidUtils;
import com.lorne.core.framework.utils.task.ConditionUtils;
import com.lorne.core.framework.utils.task.Task;
import com.lorne.tx.bean.TxTransactionInfo;
import com.lorne.tx.bean.TxTransactionLocal;
import com.lorne.tx.service.TransactionServer;
import com.lorne.tx.service.TransactionThreadService;
import com.lorne.tx.service.model.ServiceThreadModel;
import com.lorne.tx.utils.ThreadPoolSizeHelper;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 分布式事务启动开始时的业务处理
 * Created by lorne on 2017/6/8.
 */
@Service(value = "txRunningTransactionServer")
public class TxRunningTransactionServerImpl implements TransactionServer {

    private Logger logger = LoggerFactory.getLogger(TxRunningTransactionServerImpl.class);

    @Autowired
    private TransactionThreadService transactionThreadService;


    private Executor threadPool  = Executors.newFixedThreadPool(ThreadPoolSizeHelper.getInstance().getStartSize());

    @Override
    public Object execute(final ProceedingJoinPoint point, final TxTransactionInfo info) throws Throwable {
        //分布式事务开始执行
        logger.info("tx-running-start");

        final String groupId = info.getTxTransactionLocal() == null ? null : info.getTxTransactionLocal().getGroupId();

        final String taskId = KidUtils.generateShortUuid();
        final Task task = ConditionUtils.getInstance().createTask(taskId);

        threadPool.execute(new Runnable() {
            @Override
            public void run() {

                String _groupId = "";
                if (StringUtils.isEmpty(groupId)) {
                    String txGroupId = info.getTxGroupId();
                    _groupId = txGroupId;
                    if (StringUtils.isNotEmpty(txGroupId)) {
                        TxTransactionLocal txTransactionLocal = new TxTransactionLocal();
                        txTransactionLocal.setGroupId(txGroupId);
                        TxTransactionLocal.setCurrent(txTransactionLocal);
                    }
                } else {
                    _groupId = groupId;
                }


                ServiceThreadModel model = transactionThreadService.serviceInThread(info, true, _groupId, task, point);
                if (model == null) {
                    return;
                }
                logger.info("taskId-id-tx-running:" + model.getWaitTask().getKey());
                transactionThreadService.serviceWait(true, task, model);
            }
        });

        task.awaitTask();

        logger.info("tx-running-end");
        //分布式事务执行完毕
        try {
            return task.getBack().doing();
        } finally {
            task.remove();
        }
    }
}
