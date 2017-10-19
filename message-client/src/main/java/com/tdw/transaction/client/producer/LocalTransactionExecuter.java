package com.tdw.transaction.client.producer;

public interface LocalTransactionExecuter {
    LocalTransactionState executeLocalTransactionBranch(final String msgId, final Object arg);
}