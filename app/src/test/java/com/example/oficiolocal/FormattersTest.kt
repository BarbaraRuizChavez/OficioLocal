package com.example.oficiolocal

import com.example.oficiolocal.ui.components.categoryStyle
import com.example.oficiolocal.ui.components.isAvailableToday
import com.example.oficiolocal.ui.components.maxPriceOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormattersTest {
    @Test fun maxPrice_leeElNumeroMasAlto() = assertEquals(500, maxPriceOf("$200 - $500"))
    @Test fun maxPrice_sinNumeros() = assertEquals(null, maxPriceOf("Consultar"))
    @Test fun lunesASabado_incluyeMiercoles() = assertTrue(isAvailableToday("Lun - Sáb: 8:00 AM", today = 2))
    @Test fun lunesASabado_noIncluyeDomingo() = assertFalse(isAvailableToday("Lun - Sáb: 8:00 AM", today = 6))
    @Test fun oficios_tienenColoresDistintos() =
        assertNotEquals(categoryStyle("Plomería").color, categoryStyle("Electricidad").color)
}
