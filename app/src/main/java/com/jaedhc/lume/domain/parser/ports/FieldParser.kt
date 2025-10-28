package com.jaedhc.lume.domain.parser.ports

import com.jaedhc.lume.domain.parser.model.TxFields


interface FieldParser{
    fun parse(fullText:String): TxFields
}