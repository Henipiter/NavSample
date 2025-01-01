package com.example.navsample.dto

import com.example.navsample.entities.TranslateEntity
import com.example.navsample.entities.relations.ProductRichData
import com.example.navsample.entities.relations.ReceiptWithStore

class SyncChecker {
    companion object {
        fun loadRightList(
            syncMarker: SyncMarker, loadNotSynced: () -> Unit, loadOutdated: () -> Unit
        ) {
            if (syncMarker.shouldSync) {
                loadNotSynced.invoke()
            }
            if (syncMarker.shouldSync) {
                loadOutdated.invoke()
            }
        }

        fun checkIfShouldSync(entities: List<ReceiptWithStore>): SyncMarker {
            val syncMarker = SyncMarker()
            entities.forEach { entity ->
                if (!entity.isSync) {
                    syncMarker.shouldSync = true
                }
                if (entity.toUpdate || entity.toDelete) {
                    syncMarker.shouldUpdate = true
                }
                if (syncMarker.shouldSync && syncMarker.shouldUpdate) {
                    return syncMarker
                }
            }
            return syncMarker
        }

        fun checkIfShouldSync(entities: List<ProductRichData>): SyncMarker {
            val syncMarker = SyncMarker()
            entities.forEach { entity ->
                if (!entity.isSync) {
                    syncMarker.shouldSync = true
                }
                if (entity.toUpdate || entity.toDelete) {
                    syncMarker.shouldUpdate = true
                }
                if (syncMarker.shouldSync && syncMarker.shouldUpdate) {
                    return syncMarker
                }
            }
            return syncMarker
        }

        fun <T : TranslateEntity> checkIfShouldSync(entities: List<T>): SyncMarker {
            val syncMarker = SyncMarker()
            entities.forEach { entity ->
                if (!entity.isSync) {
                    syncMarker.shouldSync = true
                }
                if (entity.toUpdate || entity.toDelete) {
                    syncMarker.shouldUpdate = true
                }
                if (syncMarker.shouldSync && syncMarker.shouldUpdate) {
                    return syncMarker
                }
            }
            return syncMarker
        }

    }
}