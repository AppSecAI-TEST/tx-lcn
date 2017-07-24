package com.lorne.tx.mq.handler;

/**
 * Created by lorne on 2017/6/29.
 */

import com.alibaba.fastjson.JSONObject;
import com.lorne.core.framework.utils.task.ConditionUtils;
import com.lorne.core.framework.utils.task.IBack;
import com.lorne.core.framework.utils.task.Task;
import com.lorne.tx.manager.service.TxManagerService;
import com.lorne.tx.model.NotifyMsg;
import com.lorne.tx.mq.model.TxGroup;
import com.lorne.tx.mq.service.MQTxManagerService;
import com.lorne.tx.socket.SocketManager;
import com.lorne.tx.socket.utils.SocketUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.lang.StringUtils;

/**
 * Handles a server-side channel.
 */

@ChannelHandler.Sharable
public class TxCoreServerHandler extends ChannelInboundHandlerAdapter { // (1)

    private MQTxManagerService txManagerService;

    public TxCoreServerHandler(MQTxManagerService txManagerService) {
        this.txManagerService = txManagerService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String json = SocketUtils.getJson(msg);
        if (StringUtils.isNotEmpty(json)) {
            JSONObject jsonObject = JSONObject.parseObject(json);
            String action = jsonObject.getString("a");
            String key = jsonObject.getString("k");
            JSONObject params = JSONObject.parseObject(jsonObject.getString("p"));

            String res = "";
            switch (action) {
                //创建事务组
                case "cg": {
                    TxGroup txGroup = txManagerService.createTransactionGroup();
                    res = txGroup.toJsonString(false);
                    break;
                }
                //添加事务组
                case "atg": {
                    String groupId = params.getString("g");
                    String taskId = params.getString("t");
                    String modelName = ctx.channel().remoteAddress().toString();
                    if (StringUtils.isNotEmpty(modelName)) {
                        TxGroup txGroup = txManagerService.addTransactionGroup(groupId, taskId, modelName);
                        if(txGroup!=null) {
                            res = txGroup.toJsonString(false);
                        }
                    } else {
                        res = "";
                    }
                    break;
                }
                //修改模块信息
                case "nti": {
                    String groupId = params.getString("g");
                    String kid = params.getString("k");
                    int state = params.getInteger("s");
                    NotifyMsg resMsg = txManagerService.notifyTransactionInfo(groupId, kid, state == 1);
                    if(resMsg==null){
                        res = "";
                    }else {
                        res = resMsg.toJsonString();
                    }
                    break;
                }

                //关闭事务组
                case "ctg": {
                    String groupId = params.getString("g");
                    boolean bs = txManagerService.closeTransactionGroup(groupId);

                    res = bs ? "1" : "0";
                    break;
                }

                //检查事务组
                case "ckg": {
                    String groupId = params.getString("g");
                    String taskId = params.getString("t");
                    boolean bs = txManagerService.checkTransactionGroup(groupId,taskId);

                    res = bs ? "1" : "0";
                    break;
                }


                //心跳包
                case "h": {
                    res = "1";
                    break;
                }

                //通知事务单元
                case "t": {
                    final String data = params.getString("d");
                    Task task = ConditionUtils.getInstance().getTask(key);
                    if (task != null) {
                        task.setBack(new IBack() {
                            @Override
                            public Object doing(Object... objs) throws Throwable {
                                return data;
                            }
                        });
                        task.signalTask();
                    }
                    return;
                }

            }
            JSONObject resObj = new JSONObject();
            resObj.put("k", key);
            resObj.put("d", res);

            SocketUtils.sendMsg(ctx,resObj.toString());
        }

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

        //是否到达最大上线连接数
        if (SocketManager.getInstance().isAllowConnection()) {
            SocketManager.getInstance().addClient(ctx.channel());
        } else {
            ctx.close();
        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        SocketManager.getInstance().removeClient(ctx.channel());
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //心跳配置
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                ctx.close();
            }
        }
    }

}