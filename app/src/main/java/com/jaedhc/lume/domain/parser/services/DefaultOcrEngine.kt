package com.jaedhc.lume.domain.parser.services

import com.jaedhc.lume.domain.parser.model.OcrInput
import com.jaedhc.lume.domain.parser.model.OcrResult
import com.jaedhc.lume.domain.parser.ports.CategoryClassifier
import com.jaedhc.lume.domain.parser.ports.FieldParser
import com.jaedhc.lume.domain.parser.ports.OcrTextRecognizer

class DefaultOcrEngine(
    private val recognizer: OcrTextRecognizer,
    private val parser: FieldParser,
    private val classifier: CategoryClassifier
) {
    suspend fun analyze(input: OcrInput): OcrResult {
        val text = when(input){
            is OcrInput.PlainText -> input.text
            else -> recognizer.recognize(input)
        }
        val fields = parser.parse(text)
        val categoryId = classifier.classify(fields, text)
        return OcrResult(text = text,fields = fields, categoryId = categoryId)

    }
}