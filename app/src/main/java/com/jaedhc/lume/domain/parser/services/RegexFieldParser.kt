package com.jaedhc.lume.domain.parser.services

import com.jaedhc.lume.domain.parser.model.TxFields
import com.jaedhc.lume.domain.parser.ports.FieldParser
import java.util.Date


//FiledParser impl
class RegexFieldParser : FieldParser {

    override fun parse(fullText: String): TxFields {
        val t = fullText.replace(",","").trim()

        val amount = Regex("""\$(\d+(?:\.\d{1,2})?)""")
            .findAll(t)
            .mapNotNull { it.groupValues.getOrNull(1)?.toDoubleOrNull() }
            .maxOrNull() ?: 0.0

        val currency = when {
            Regex("""\bMXN|\$""", RegexOption.IGNORE_CASE).containsMatchIn(t) -> "MXN"
            Regex("""\bUSD\b""", RegexOption.IGNORE_CASE).containsMatchIn(t) -> "USD"
            else -> "MXN"
        }

        val dateRaw = Regex("""\b(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})\b""").find(t)?.value
            ?: Regex("""\b(\d{1,2}\s+(?:ene|feb|mar|abr|may|jun|jul|ago|sep|oct|nov|dic)\w*\s+\d{2,4})\b""",
                RegexOption.IGNORE_CASE).find(t)?.value
        val dateIso = dateRaw?.let { normalizeToIso(it) } ?: normalizeToIso(Date().toString())

        val merchant = Regex("""(?i)(?:comercio|beneficiario|establecimiento|receptor|concepto)\s*:\s*(.+)""")
            .find(t)?.groupValues?.getOrNull(1)?.lineOrFirstWord() ?: ""

        val concept = Regex("""(?i)concepto\s*:\s*(.+)""")
            .find(t)?.groupValues?.getOrNull(1)?.lineOrFirstWord()
            ?: merchant

        return TxFields(amount, currency, dateIso, merchant, concept)
    }

    private fun String.lineOrFirstWord() =
        this.lines().firstOrNull()?.trim() ?: this.trim()

    private fun normalizeToIso(s: String): String {
        val m1 = Regex("""(\d{1,2})[/-](\d{1,2})[/-](\d{2,4})""").matchEntire(s)
        if (m1 != null) {
            val d = m1.groupValues[1].padStart(2, '0')
            val mo = m1.groupValues[2].padStart(2, '0')
            val y = m1.groupValues[3].let { if (it.length==2) "20$it" else it }
            return "$y-$mo-$d"
        }
        return ""
    }

}