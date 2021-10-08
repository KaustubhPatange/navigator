package com.kpstv.navigation.compose.sample.ui.componenets

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommonItem(val name: String, val age: Int) : Parcelable

val sampleCommonItems = listOf(
    CommonItem("Polly", 4),
    CommonItem("Ollie", 6),
    CommonItem("Hershey", 4),
    CommonItem("Annie", 3),
    CommonItem("Kate", 5),
    CommonItem("Riley", 10),
    CommonItem("Eddie", 7),
    CommonItem("Sam", 9),
    CommonItem("Hunter", 5),
    CommonItem("Zoe", 2),
    CommonItem("Ava", 6),
    CommonItem("Gracie", 6),
    CommonItem("Liberty", 6),
    CommonItem("Isabella", 9),
    CommonItem("Luna", 3),
    CommonItem("Jada", 6),
    CommonItem("Abby", 4),
    CommonItem("Harley", 8),
    CommonItem("Henry", 3),
)