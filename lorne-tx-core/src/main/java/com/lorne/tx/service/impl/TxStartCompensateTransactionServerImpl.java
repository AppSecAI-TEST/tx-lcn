package com.lorne.tx.service.impl;

import com.lorne.tx.bean.TxTransactionInfo;
import com.lorne.tx.bean.TxTransactionLocal;
import com.lorne.tx.compensate.service.impl.CompensateServiceImpl;
import com.lorne.tx.service.TransactionServer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Created by yuliang on 2017/7/11.
 */
@Service(value = "txStartCompensateTransactionServer")
public class TxStartCompensateTransactionServerImpl implements TransactionServer {



    @Autowired
    private PlatformTransactionManager txManager;


    @Override
    public Object execute(ProceedingJoinPoint point, TxTransactionInfo info) throws Throwable {
        TxTransactionLocal txTransactionLocal = new TxTransactionLocal();
        txTransactionLocal.setHasCompensate(true);
        txTransactionLocal.setGroupId(CompensateServiceImpl.COMPENSATE_KEY);
        TxTransactionLocal.setCurrent(txTransactionLocal);

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = txManager.getTransaction(def);
        try {
            Object obj =  point.proceed();
            txManager.commit(status);
            return obj;
        }catch (Throwable e) {
            txManager.rollback(status);
            throw e;
        }finally {
            if(txTransactionLocal!=null){
                TxTransactionLocal.setCurrent(null);
            }
        }
    }
}
