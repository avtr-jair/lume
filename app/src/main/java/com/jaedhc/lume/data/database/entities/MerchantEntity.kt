package com.jaedhc.lume.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "merchant",
    indices = [
        Index("nameTok"),            // búsquedas por igualdad (opcional)
        Index("defaultCategoryId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["defaultCategoryId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.NO_ACTION
        )
    ]
)
data class MerchantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nameEnc: String?,
    val nameTok: String?,
    val defaultCategoryId: Long?
)
