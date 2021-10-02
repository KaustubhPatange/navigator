package com.kpstv.navigation.compose.internels

internal fun<K, V> Map<K,V>.lastValue(): V? = get(keys.lastOrNull())