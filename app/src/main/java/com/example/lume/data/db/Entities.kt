package com.example.lume.data.db

import androidx.room.*

@Entity(
    tableName = "categories"
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String? = null
)

@Entity(
    tableName = "accounts"
)
data class AccountEntity(
    @PrimaryKey val id: String,
    val bankName: String,
    val last4: String,
    val label: String? = null
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("accountId")]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val currency: String,
    val dateIso: String,
    val merchant: String?,
    val concept: String?,
    val categoryId: String,
    val accountId: String?,
    val type: String, // "ingreso" or "egreso"
    val isSubscription: Boolean,
    val note: String?,
    val createdAt: Long = System.currentTimeMillis()
)
