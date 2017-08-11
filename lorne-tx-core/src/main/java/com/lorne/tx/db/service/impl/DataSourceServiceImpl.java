package com.lorne.tx.db.service.impl;

import com.lorne.core.framework.utils.http.HttpUtils;
import com.lorne.core.framework.utils.task.Task;
import com.lorne.tx.compensate.service.CompensateService;
import com.lorne.tx.db.service.DataSourceService;
import com.lorne.tx.mq.service.MQTxManagerService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * create by lorne on 2017/7/29
 */
@Service
public class DataSourceServiceImpl implements DataSourceService {


    @Autowired
    private MQTxManagerService txManagerService;

    @Autowired
    private CompensateService compensateService;


    @Override
    public void schedule(String groupId,List<String> compensates, Task waitTask) {
        String waitTaskId = waitTask.getKey();
        int rs = txManagerService.checkTransactionInfo(groupId, waitTaskId);
        if (rs == 1 || rs == 0) {
            waitTask.setState(rs);
            waitTask.signalTask();
            //clear
            txManagerService.httpClearTransactionInfo(groupId,waitTaskId,true);

            return;
        }
        if(rs==-2) {
            rs = txManagerService.httpCheckTransactionInfo(groupId, waitTaskId);
            if (rs == 1 || rs == 0) {
                waitTask.setState(rs);
                waitTask.signalTask();
                //clear

                txManagerService.httpClearTransactionInfo(groupId, waitTaskId, false);
                return;
            }
        }

        //添加到补偿队列
        waitTask.setState(-100);
        waitTask.signalTask();

    }

    @Override
    public void deleteCompensates(List<String> compensates) {
        for(String compensateId:compensates){
            if(StringUtils.isNotEmpty(compensateId)) {
                deleteCompensateId(compensateId);
            }
        }
    }

    @Override
    public void deleteCompensateId(String compensateId) {
        compensateService.deleteTransactionInfo(compensateId);
    }
}
