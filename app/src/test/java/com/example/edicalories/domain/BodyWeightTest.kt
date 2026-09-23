package com.example.edicalories.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BodyWeightTest {

    @Test
    fun parseToTenths_acceptsDotAndComma() {
        assertEquals(724, BodyWeight.parseToTenths("72.4"))
        assertEquals(724, BodyWeight.parseToTenths("72,4"))
        assertEquals(720, BodyWeight.parseToTenths(" 72 "))
        assertEquals(200, BodyWeight.parseToTenths("20.0"))
        assertEquals(4000, BodyWeight.parseToTenths("400.0"))
    }

    @Test
    fun parseToTenths_rejectsEmptyAndNonNumeric() {
        assertNull(BodyWeight.parseToTenths(""))
        assertNull(BodyWeight.parseToTenths("   "))
        assertNull(BodyWeight.parseToTenths("abc"))
        assertNull(BodyWeight.parseToTenths("72.45"))
        assertNull(BodyWeight.parseToTenths("72.4.1"))
    }

    @Test
    fun parseToTenths_rejectsOutOfRange() {
        assertNull(BodyWeight.parseToTenths("19.9"))
        assertNull(BodyWeight.parseToTenths("400.1"))
        assertNull(BodyWeight.parseToTenths("0"))
        assertNull(BodyWeight.parseToTenths("999"))
    }

    @Test
    fun formatKg_keepsOneDecimalWhenNeeded() {
        assertEquals("72", BodyWeight.formatKg(720))
        assertEquals("72.4", BodyWeight.formatKg(724))
        assertEquals("20", BodyWeight.formatKg(200))
    }
}
