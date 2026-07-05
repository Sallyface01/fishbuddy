package com.fishbuddy.app.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [30])
class CameraServiceTest {

    /** Create a solid-color test bitmap. */
    private fun createTestBitmap(width: Int, height: Int, quality: Int = 90): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(0xFF4488CC.toInt())
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        bitmap.recycle()
        return out.toByteArray()
    }

    // === compressImage ===

    @Test
    fun `compressImage reduces large image`() {
        val large = createTestBitmap(4096, 3072, 95)
        val compressed = CameraService.compressImage(large, maxWidth = 2048, quality = 70)

        val bitmap = BitmapFactory.decodeByteArray(compressed, 0, compressed.size)
        assertNotNull("Compressed data should produce a valid bitmap", bitmap)
        assertTrue("Width should be ≤ 2048, got ${bitmap.width}", bitmap.width <= 2048)
        assertTrue("Height should be ≤ 2048, got ${bitmap.height}", bitmap.height <= 2048)
        bitmap.recycle()
    }

    @Test
    fun `compressImage keeps small image unchanged in size`() {
        val small = createTestBitmap(200, 150, 90)
        val compressed = CameraService.compressImage(small, maxWidth = 2048, quality = 70)

        val bitmap = BitmapFactory.decodeByteArray(compressed, 0, compressed.size)
        assertNotNull(bitmap)
        // Small image should not be upscaled
        assertEquals(200, bitmap.width)
        assertEquals(150, bitmap.height)
        bitmap.recycle()
    }

    @Test
    fun `compressImage handles minimum width`() {
        val data = createTestBitmap(800, 600, 90)
        val compressed = CameraService.compressImage(data, maxWidth = 100, quality = 50)

        val bitmap = BitmapFactory.decodeByteArray(compressed, 0, compressed.size)
        assertNotNull(bitmap)
        assertTrue("Width should be ≤ 100, got ${bitmap.width}", bitmap.width <= 100)
        bitmap.recycle()
    }

    @Test
    fun `compressImage does not crash on minimal input`() {
        // 1x1 pixel image
        val tiny = createTestBitmap(1, 1, 90)
        val compressed = CameraService.compressImage(tiny, maxWidth = 2048, quality = 70)
        assertTrue(compressed.isNotEmpty())
    }

    // === makeThumbnail ===

    @Test
    fun `makeThumbnail produces small output`() {
        val data = createTestBitmap(2048, 1536, 90)
        val thumb = CameraService.makeThumbnail(data, size = 200)

        val bitmap = BitmapFactory.decodeByteArray(thumb, 0, thumb.size)
        assertNotNull(bitmap)
        assertTrue("Thumb width should be ≤ 200, got ${bitmap.width}", bitmap.width <= 200)
        assertTrue("Thumb height should be ≤ 200, got ${bitmap.height}", bitmap.height <= 200)
        bitmap.recycle()
    }

    @Test
    fun `makeThumbnail preserves aspect ratio`() {
        // Wide image: 2048x1024 → thumb should also be wider than tall
        val wideBitmap = Bitmap.createBitmap(2048, 1024, Bitmap.Config.ARGB_8888)
        wideBitmap.eraseColor(0xFF4488CC.toInt())
        val out = ByteArrayOutputStream()
        wideBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        wideBitmap.recycle()

        val thumb = CameraService.makeThumbnail(out.toByteArray(), size = 200)
        val bitmap = BitmapFactory.decodeByteArray(thumb, 0, thumb.size)
        assertNotNull(bitmap)
        assertTrue("Wide thumb should be wider than tall", bitmap.width > bitmap.height)
        bitmap.recycle()
    }

    @Test
    fun `makeThumbnail already small image stays small`() {
        val data = createTestBitmap(50, 50, 90)
        val thumb = CameraService.makeThumbnail(data, size = 200)

        val bitmap = BitmapFactory.decodeByteArray(thumb, 0, thumb.size)
        assertNotNull(bitmap)
        assertEquals(50, bitmap.width)
        assertEquals(50, bitmap.height)
        bitmap.recycle()
    }
}
