package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

@ApplicationScoped
public class StoreChangedObserver {

    @Inject
    LegacyStoreManagerGateway legacyStoreManagerGateway;

    public void onStoreChanged(
            @Observes(during = TransactionPhase.AFTER_SUCCESS)
            StoreChanged event) {

        if (event.changeType == StoreChanged.ChangeType.CREATE) {
            legacyStoreManagerGateway.createStoreOnLegacySystem(event.store);
        } else {
            legacyStoreManagerGateway.updateStoreOnLegacySystem(event.store);
        }
    }
}