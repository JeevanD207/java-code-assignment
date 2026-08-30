package com.fulfilment.application.monolith.stores;

public class StoreChanged {

    public final Store store;
    public final ChangeType changeType;

    public StoreChanged(Store store, ChangeType changeType) {
        this.store = store;
        this.changeType = changeType;
    }

    public enum ChangeType {
        CREATE,
        UPDATE
    }
}