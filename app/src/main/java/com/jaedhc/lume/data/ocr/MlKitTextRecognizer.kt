package com.jaedhc.lume.data.ocr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.jaedhc.lume.domain.parser.model.OcrInput
import com.jaedhc.lume.domain.parser.ports.OcrTextRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MlKitTextRecognizer @Inject constructor() : OcrTextRecognizer {

    override suspend fun recognize(input: OcrInput): String = when (input) {
        is OcrInput.PlainText -> input.text
        is OcrInput.Base64    -> recognizeFromBase64(input.b64)
        is OcrInput.Bytes     -> recognizeFromBytes(input.data)
    }

    private suspend fun recognizeFromBase64(b64: String): String =
        recognizeFromBytes(Base64.decode(b64, Base64.DEFAULT))

    private suspend fun recognizeFromBytes(bytes: ByteArray): String =
        withContext(Dispatchers.Default) {
            // Downscale básico para imágenes MUY grandes (evita OOM en algunos devices)
            val bmp = decodeDownsampled(bytes, maxDim = 2048) ?: return@withContext ""
            try {
                val image = InputImage.fromBitmap(bmp, /*rotationDegrees=*/0)
                val client = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val result = client.process(image).await()
                result.text ?: ""
            } finally {
                bmp.recycle()
            }
        }

    /** Decodifica con downsample si alguna dimensión excede maxDim */
    private fun decodeDownsampled(data: ByteArray, maxDim: Int): Bitmap? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(data, 0, data.size, opts)
        val (w, h) = opts.outWidth to opts.outHeight
        if (w <= 0 || h <= 0) return null

        var sample = 1
        var scaledW = w
        var scaledH = h
        while (scaledW > maxDim || scaledH > maxDim) {
            sample *= 2
            scaledW = w / sample
            scaledH = h / sample
        }

        return BitmapFactory.decodeByteArray(
            data, 0, data.size,
            BitmapFactory.Options().apply { inSampleSize = sample.coerceAtLeast(1) }
        )
    }
}