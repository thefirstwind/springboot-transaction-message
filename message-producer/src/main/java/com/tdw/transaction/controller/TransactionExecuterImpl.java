package com.tdw.transaction.controller;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tdw.transaction.client.producer.LocalTransactionExecuter;
import com.tdw.transaction.client.producer.LocalTransactionState;


/**
 * 业务处理代码
 *
 */
public class TransactionExecuterImpl implements LocalTransactionExecuter {
	private static final Logger logger = LoggerFactory.getLogger(TransactionExecuterImpl.class);
    Random random = new Random();

    @Override
    public LocalTransactionState executeLocalTransactionBranch(final String msgId, final Object arg){
    	int value = random.nextInt(300);


        if ((value % 5) == 0) {
        	logger.info("TransactionExecuterImpl ROLLBACK_MESSAGE! =============>  " + msgId + "\n");
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
        
        logger.info("TransactionExecuterImpl COMMIT_MESSAGE! =============>  " + msgId + "\n");
        return LocalTransactionState.COMMIT_MESSAGE;
    }
}
