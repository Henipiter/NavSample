package com.example.navsample.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.navsample.entities.Receipt
import com.example.navsample.entities.Store

data class ReceiptWithStore(
    @Embedded val store: Store,
    @Relation(
        parentColumn = "nip",
        entityColumn = "nip"
    )
    val receipt: Receipt
)