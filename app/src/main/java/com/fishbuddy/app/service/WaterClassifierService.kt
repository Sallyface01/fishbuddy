package com.fishbuddy.app.service

import android.graphics.BitmapFactory
import com.fishbuddy.app.domain.model.WaterBodyType
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** ML Kit image labeling → water body type classification. */
class WaterClassifierService {

    suspend fun classify(imageData: ByteArray): Pair<WaterBodyType, Float> {
        val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        return suspendCancellableCoroutine { cont ->
            labeler.process(inputImage)
                .addOnSuccessListener { labels ->
                    val scores = mutableMapOf<WaterBodyType, Float>()
                    for (label in labels.take(20)) {
                        for (type in WaterBodyType.entries) {
                            if (type == WaterBodyType.UNKNOWN) continue
                            for (kw in keywordsFor(type)) {
                                if (label.text.lowercase().contains(kw)) {
                                    scores[type] = (scores[type] ?: 0f) + label.confidence
                                    break
                                }
                            }
                        }
                    }
                    val best = scores.maxByOrNull { it.value }
                    if (best != null && best.value > 0.1f) cont.resume(best.toPair())
                    else cont.resume(WaterBodyType.LAKE to 0f)
                }
                .addOnFailureListener { cont.resume(WaterBodyType.LAKE to 0f) }
        }
    }

    private fun keywordsFor(type: WaterBodyType): List<String> = when (type) {
        WaterBodyType.RIVER -> listOf("river", "waterway", "canal", "rapids", "waterfall")
        WaterBodyType.LAKE -> listOf("lake", "loch", "body_of_water")
        WaterBodyType.POND -> listOf("pond", "pool", "swamp", "marsh", "wetland", "lagoon")
        WaterBodyType.RESERVOIR -> listOf("reservoir", "dam", "basin")
        WaterBodyType.STREAM -> listOf("stream", "creek", "brook", "rivulet")
        WaterBodyType.UNKNOWN -> emptyList()
    }
}
