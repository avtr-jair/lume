package com.jaedhc.lume.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.jaedhc.lume.data.database.converters.DbConverters
import java.util.Date

@Entity(
    tableName = "tx",
    indices = [
        Index("date"),
        Index("categoryId"),
        Index("income"),
        Index("sourceAccId"),
        Index("sourceAccBankId"),
        Index("merchantId"),
        Index("amountMinor"),
        Index("createdAt"),
        Index("updatedAt"),
        Index(value = ["hashMerchant"], unique = true) // idempotencia
    ],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceAccId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = MerchantEntity::class,
            parentColumns = ["id"],
            childColumns = ["merchantId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.NO_ACTION
        )

        // OPCIONAL: si quieres FK directa al banco para sourceAccBankId, descomenta:
        // ,
        // ForeignKey(
        //     entity = BankEntity::class,
        //     parentColumns = ["id"],
        //     childColumns = ["sourceAccBankId"],
        //     onDelete = ForeignKey.NO_ACTION,
        //     onUpdate = ForeignKey.NO_ACTION
        // )
    ]
)
@TypeConverters(DbConverters::class)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,  // ← autogen con Long
    val date: Date,

    val merchantId: Long?,          // ← FK a merchant (Long)
    val conceptEnc: String?,        // AES-GCM
    val conceptTok: String?,        // HMAC si haces igualdad exacta

    val amountMinor: Long,
    val currency: String,

    val categoryId: Long,           // ← FK a category (Long)
    val income: Boolean,

    val sourceAccId: Long?,         // ← FK a account.id
    val sourceAccBankId: Long?,     // ← denormalizado para filtro rápido por banco

    val hashMerchant: String,       // UNIQUE (idempotencia)
    val createdAt: Date,
    val updatedAt: Date
)
