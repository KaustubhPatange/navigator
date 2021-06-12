package com.kpstv.navigator.compose.sample.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GalleryItem(val name: String, val age: Int) : Parcelable

val galleryItems = listOf(
    GalleryItem("Polly", 4),
    GalleryItem("Ollie", 6),
    GalleryItem("Hershey", 4),
    GalleryItem("Annie", 3),
    GalleryItem("Kate", 5),
    GalleryItem("Riley", 10),
    GalleryItem("Eddie", 7),
    GalleryItem("Sam", 9),
    GalleryItem("Hunter", 5),
    GalleryItem("Zoe", 2),
    GalleryItem("Ava", 6),
    GalleryItem("Gracie", 6),
)