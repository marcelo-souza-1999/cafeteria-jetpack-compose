package com.targaryen.cafeteria.feature_catalog.catalog.data.mapper

import com.google.firebase.firestore.DocumentSnapshot
import com.targaryen.cafeteria.core_designsystem.model.CatalogCategories
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductMapperTest {
    @Test
    fun toProductEntity_shouldMapToDragonFire_whenDescriptionContainsHotOrFire() {
        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.getString("category") } returns "bebidas"
        every { snapshot.getString("name") } returns "Mocha do Dragão"
        every { snapshot.getString("description") } returns "Mocha premium servido quente e com especiarias."
        every { snapshot.get("tags") } returns null
        every { snapshot.getString("id") } returns "mocha_dragao"
        every { snapshot.id } returns "mocha_dragao"
        every { snapshot.getDouble("price") } returns 14.90
        every { snapshot.getString("imageUrl") } returns ""

        val entity = snapshot.toProductEntity()

        assertEquals(CatalogCategories.DRAGON_FIRE, entity.category)
    }

    @Test
    fun toProductEntity_shouldMapToIceBreath_whenDescriptionContainsIceOrGlacial() {
        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.getString("category") } returns "bebidas"
        every { snapshot.getString("name") } returns "Chá da Muralha"
        every { snapshot.getString("description") } returns "Chá gelado e refrescante da patrulha."
        every { snapshot.get("tags") } returns null
        every { snapshot.getString("id") } returns "cha_muralha"
        every { snapshot.id } returns "cha_muralha"
        every { snapshot.getDouble("price") } returns 10.00
        every { snapshot.getString("imageUrl") } returns ""

        val entity = snapshot.toProductEntity()

        assertEquals(CatalogCategories.ICE_BREATH, entity.category)
    }
}
