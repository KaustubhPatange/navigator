package com.kpstv.navigation.lint.utils

import com.intellij.psi.PsiType

private const val ACTIVITY_CLASS = "androidx.appcompat.app.AppCompatActivity"
private const val VALUE_FRAGMENT_CLASS = "com.kpstv.navigation.ValueFragment"

fun PsiType.isActivity(): Boolean {
    if (canonicalText == ACTIVITY_CLASS) return true
    if (superTypes.isNotEmpty()) {
        superTypes.forEach { if (it.isActivity()) return true }
    }
    return false
}

fun PsiType.isValueFragment(): Boolean {
    if (canonicalText == VALUE_FRAGMENT_CLASS) return true
    if (superTypes.isNotEmpty()) {
        superTypes.forEach { if (it.isValueFragment()) return true }
    }
    return false
}