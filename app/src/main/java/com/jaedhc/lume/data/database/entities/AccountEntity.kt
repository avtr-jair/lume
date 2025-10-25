package com.jaedhc.lume.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "account",
    indices = [
        Index("bancoId"),
        Index(value = ["bancoId", "last4_tok"], unique = true) // (banco + last4) no se repite
    ],
    foreignKeys = [
        ForeignKey(
            entity = BankEntity::class,
            parentColumns = ["id"],
            childColumns = ["bancoId"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.NO_ACTION
        )
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bancoId: Long,

    val last4_tok: String,
    val last4_enc: String,

    val labelEnc: String,
    val isActive: Boolean = true
)