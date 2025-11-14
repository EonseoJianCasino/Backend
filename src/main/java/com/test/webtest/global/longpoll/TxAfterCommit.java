package com.test.webtest.global.longpoll;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public final class TxAfterCommit {
    private TxAfterCommit() {}

    public static void run(Runnable action) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {action.run();}
        });
    }
}
