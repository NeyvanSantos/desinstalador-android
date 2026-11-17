package com.neyvan.desinstalador.accessibility

data class AccessibilityTextEvidence(
    val text: String,
    val isVisibleToUser: Boolean,
    val windowId: Int
)

internal fun relevantVisibleTexts(
    evidence: Collection<AccessibilityTextEvidence>,
    expectedWindowId: Int
): List<String> = evidence.asSequence()
    .filter { it.isVisibleToUser && it.windowId == expectedWindowId }
    .map(AccessibilityTextEvidence::text)
    .toList()
