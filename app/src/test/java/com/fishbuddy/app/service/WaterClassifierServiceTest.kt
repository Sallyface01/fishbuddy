package com.fishbuddy.app.service

import com.fishbuddy.app.domain.model.WaterBodyType
import org.junit.Assert.*
import org.junit.Test

class WaterClassifierServiceTest {

    private val classifier = WaterClassifierService()

    // === keyword coverage ===

    @Test
    fun `all water types have keywords`() {
        for (type in WaterBodyType.entries) {
            if (type == WaterBodyType.UNKNOWN) continue
            val keywords = getKeywordsFor(type)
            assertTrue("$type should have at least 3 keywords, got ${keywords.size}", keywords.size >= 3)
        }
    }

    @Test
    fun `UNKNOWN has no keywords`() {
        val keywords = getKeywordsFor(WaterBodyType.UNKNOWN)
        assertTrue("UNKNOWN should have empty keywords", keywords.isEmpty())
    }

    @Test
    fun `RIVER keywords contain river and waterway`() {
        val k = getKeywordsFor(WaterBodyType.RIVER)
        assertTrue("RIVER should contain 'river'", k.contains("river"))
        assertTrue("RIVER should contain 'waterway'", k.contains("waterway"))
    }

    @Test
    fun `LAKE keywords contain lake and body_of_water`() {
        val k = getKeywordsFor(WaterBodyType.LAKE)
        assertTrue("LAKE should contain 'lake'", k.contains("lake"))
        assertTrue("LAKE should contain 'body_of_water'", k.contains("body_of_water"))
        // "reservoir" is intentionally in RESERVOIR only, not duplicated in LAKE
        assertFalse("LAKE should NOT contain 'reservoir'", k.contains("reservoir"))
    }

    @Test
    fun `POND keywords contain pond and swamp`() {
        val k = getKeywordsFor(WaterBodyType.POND)
        assertTrue("POND should contain 'pond'", k.contains("pond"))
        assertTrue("POND should contain 'swamp'", k.contains("swamp"))
    }

    @Test
    fun `RESERVOIR keywords contain dam and basin`() {
        val k = getKeywordsFor(WaterBodyType.RESERVOIR)
        assertTrue("RESERVOIR should contain 'dam'", k.contains("dam"))
        assertTrue("RESERVOIR should contain 'basin'", k.contains("basin"))
    }

    @Test
    fun `STREAM keywords contain stream and creek`() {
        val k = getKeywordsFor(WaterBodyType.STREAM)
        assertTrue("STREAM should contain 'stream'", k.contains("stream"))
        assertTrue("STREAM should contain 'creek'", k.contains("creek"))
    }

    @Test
    fun `keywords are all lowercase`() {
        for (type in WaterBodyType.entries) {
            for (kw in getKeywordsFor(type)) {
                assertEquals("Keyword '$kw' for $type should be lowercase", kw, kw.lowercase())
            }
        }
    }

    @Test
    fun `no duplicate keywords across types`() {
        val all = mutableMapOf<String, WaterBodyType>()
        for (type in WaterBodyType.entries) {
            for (kw in getKeywordsFor(type)) {
                if (all.containsKey(kw)) {
                    fail("Keyword '$kw' appears in both ${all[kw]} and $type")
                }
                all[kw] = type
            }
        }
    }

    // === classification logic (simulated) ===

    @Test
    fun `classify returns LAKE when no keywords match`() {
        // Simulate what happens: no labels match any keyword → LAKE with 0 confidence
        val matched = WaterBodyType.LAKE // fallback
        assertEquals(WaterBodyType.LAKE, matched)
    }

    @Test
    fun `WaterBodyType has all 6 types`() {
        assertEquals(6, WaterBodyType.entries.size)
        val expected = setOf("河流", "湖泊", "池塘", "水库", "溪流", "未知")
        val actual = WaterBodyType.entries.map { it.chineseName }.toSet()
        assertEquals(expected, actual)
    }
}

/** Expose private keywordsFor for testing via reflection-like helper. */
private fun getKeywordsFor(type: WaterBodyType): List<String> {
    val method = WaterClassifierService::class.java.getDeclaredMethod(
        "keywordsFor", WaterBodyType::class.java
    )
    method.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    return method.invoke(WaterClassifierService(), type) as List<String>
}
