package com.example

import com.example.data.model.LedgerType
import com.example.data.service.OcrKhataParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrKhataParserTest {

    private val parser = OcrKhataParser()

    @Test
    fun testParseKhataText_extractsRowsInOrder() {
        val rawInput = """
            2026-08-28 Suresh Verma ration aata dal Rs 1450 udhaar
            2026-08-28 Daily cash counter sales ₹3280 jama
            2026-08-28 Shiv Shakti Traders wholesale oil tins ₹2600 kharch
        """.trimIndent()

        val parsed = parser.parseKhataText(rawInput)

        assertEquals(3, parsed.size)

        val first = parsed[0]
        assertEquals("2026-08-28", first.date)
        assertEquals(1450.0, first.amount, 0.01)
        assertEquals(LedgerType.DEBIT, first.type)

        val second = parsed[1]
        assertEquals("2026-08-28", second.date)
        assertEquals(3280.0, second.amount, 0.01)
        assertEquals(LedgerType.CREDIT, second.type)

        val third = parsed[2]
        assertEquals("2026-08-28", third.date)
        assertEquals(2600.0, third.amount, 0.01)
        assertEquals(LedgerType.DEBIT, third.type)
    }

    @Test
    fun testParseCsvText_parsesCsvRows() {
        val csvInput = """
            Date, Description, Amount, Type
            2026-08-28, Suresh Verma ration, 1450, Debit
            2026-08-28, Daily Cash Counter, 3280, Credit
        """.trimIndent()

        val parsed = parser.parseCsvText(csvInput)

        assertEquals(2, parsed.size)
        assertEquals(1450.0, parsed[0].amount, 0.01)
        assertEquals(LedgerType.DEBIT, parsed[0].type)
        assertEquals(3280.0, parsed[1].amount, 0.01)
        assertEquals(LedgerType.CREDIT, parsed[1].type)
    }
}
