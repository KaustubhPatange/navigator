package com.kpstv.navigation.internals

import com.kpstv.navigation.FragClazz

internal data class BackStackRecord(
    val name: String,
    val qualifiedName: String
) {
    constructor(tag: String, clazz: FragClazz) : this(tag, clazz.qualifiedName!!)
}