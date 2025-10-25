package com.jaedhc.lume.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "bank", indices = [Index(value = ["identifier"], unique = true)])
data class BankEntity(
    @PrimaryKey(autoGenerate = true)
    val id:  Long = 0,                     //Identificador único
    val identifier: String,             //Nombre del banco
)