package com.lorne.tx.service;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Created by lorne on 2017/7/1.
 */
public interface AspectBeforeService {

    Object around(String groupId,int maxTimeOut, ProceedingJoinPoint point) throws Throwable;
}
