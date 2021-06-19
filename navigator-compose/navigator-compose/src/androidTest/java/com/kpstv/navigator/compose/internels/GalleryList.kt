package com.kpstv.navigator.compose.internels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
public data class GalleryItem(val name: String, val age: Int) : Parcelable

public val galleryItems: List<GalleryItem> = listOf(
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
    GalleryItem("Liberty", 6),
    GalleryItem("Isabella", 9),
    GalleryItem("Luna", 3),
    GalleryItem("Jada", 6),
    GalleryItem("Abby", 4),
    GalleryItem("Harley", 8),
    GalleryItem("Henry", 3),
)