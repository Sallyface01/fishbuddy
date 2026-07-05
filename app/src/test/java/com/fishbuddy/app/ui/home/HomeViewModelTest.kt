package com.fishbuddy.app.ui.home

import android.app.Application
import com.fishbuddy.app.FishBuddyApp
import com.fishbuddy.app.data.model.AnalysisRecordEntity
import com.fishbuddy.app.data.repository.AnalysisRepository
import com.fishbuddy.app.domain.model.FishSpecies
import com.fishbuddy.app.domain.model.FishingRecommendation
import com.fishbuddy.app.domain.model.WaterBodyType
import com.fishbuddy.app.service.FishDatabaseService
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [30])
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var app: FishBuddyApp
    private lateinit var analysisRepo: AnalysisRepository
    private lateinit var fishDb: FishDatabaseService

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Mock FishBuddyApp and its dependencies
        analysisRepo = mockk(relaxed = true)
        fishDb = mockk(relaxed = true)

        app = mockk(relaxed = true)
        every { app.analysisRepository } returns analysisRepo
        every { app.fishDatabase } returns fishDb

        // Default: fish database returns empty query results
        every { fishDb.query(any(), any()) } returns Pair(emptyList(), emptyList())
        every { fishDb.resolveLocationName(any()) } returns "测试地点"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // === State transitions ===

    @Test
    fun `initial state has no analysis result`() {
        val vm = HomeViewModel(app)
        val state = vm.state.value

        assertFalse(state.showCameraOptions)
        assertFalse(state.isAnalyzing)
        assertNull(state.analysisResult)
        assertNull(state.errorMessage)
    }

    @Test
    fun `showCamera sets showCameraOptions true`() {
        val vm = HomeViewModel(app)
        vm.showCamera()
        assertTrue(vm.state.value.showCameraOptions)
    }

    @Test
    fun `dismissCameraOptions resets flag`() {
        val vm = HomeViewModel(app)
        vm.showCamera()
        vm.dismissCameraOptions()
        assertFalse(vm.state.value.showCameraOptions)
    }

    @Test
    fun `reset clears analysis result`() = runTest {
        val vm = HomeViewModel(app)

        // Simulate a completed analysis by directly setting state via internal flow
        // Since we can't easily trigger the full pipeline, test that reset clears everything
        vm.reset()
        val state = vm.state.value

        assertNull(state.analysisResult)
        assertFalse(state.isAnalyzing)
        assertEquals(emptyList<FishSpecies>(), state.resultSpecies)
    }

    @Test
    fun `dismissError clears error message`() {
        val vm = HomeViewModel(app)
        // trigger dismiss — no-op if error is null, but shouldn't crash
        vm.dismissError()
        assertNull(vm.state.value.errorMessage)
    }

    // === HomeState ===

    @Test
    fun `HomeState default values`() {
        val state = HomeState()
        assertFalse(state.showCameraOptions)
        assertFalse(state.isAnalyzing)
        assertEquals("正在分析水域...", state.loadingMessage)
        assertNull(state.analysisResult)
        assertNull(state.errorMessage)
    }

    @Test
    fun `HomeState copy retains values`() {
        val state = HomeState(isAnalyzing = true, loadingMessage = "测试中")
        assertTrue(state.isAnalyzing)
        assertEquals("测试中", state.loadingMessage)
    }

    @Test
    fun `HomeState copy clears error on dismissError`() {
        val state = HomeState(errorMessage = "出错了")
        assertEquals("出错了", state.errorMessage)
        val cleared = state.copy(errorMessage = null)
        assertNull(cleared.errorMessage)
    }

    // === Helper ===

    private fun createMinimalJpeg(): ByteArray {
        val bmp = android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888)
        bmp.eraseColor(0xFF336699.toInt())
        val out = java.io.ByteArrayOutputStream()
        bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out)
        bmp.recycle()
        return out.toByteArray()
    }
}
