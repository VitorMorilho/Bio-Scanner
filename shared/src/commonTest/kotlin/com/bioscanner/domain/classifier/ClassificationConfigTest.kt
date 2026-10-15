package com.bioscanner.domain.classifier

import com.bioscanner.domain.model.ClassificationCandidate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClassificationConfigTest {

    @Test
    fun `candidate exactly at threshold is confident`() {
        val candidate = ClassificationCandidate("x", ClassificationConfig.MIN_CONFIDENCE_THRESHOLD)
        assertTrue(ClassificationConfig.isConfident(candidate))
    }

    @Test
    fun `candidate just below threshold is not confident`() {
        val candidate = ClassificationCandidate("x", ClassificationConfig.MIN_CONFIDENCE_THRESHOLD - 0.01f)
        assertFalse(ClassificationConfig.isConfident(candidate))
    }

    @Test
    fun `candidate just above threshold is confident`() {
        val candidate = ClassificationCandidate("x", ClassificationConfig.MIN_CONFIDENCE_THRESHOLD + 0.01f)
        assertTrue(ClassificationConfig.isConfident(candidate))
    }
}
