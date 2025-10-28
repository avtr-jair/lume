package com.jaedhc.lume.domain.parser.ports

import com.jaedhc.lume.domain.parser.model.OcrInput

interface OcrTextRecognizer {
    suspend fun recognize(input: OcrInput): String
}