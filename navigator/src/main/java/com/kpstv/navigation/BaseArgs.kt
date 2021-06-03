package com.kpstv.navigation

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Extend this class to have generic typed arguments for [ValueFragment].
 *
 * @see <a href="https://github.com/KaustubhPatange/navigator/wiki/Quick-Tutorials#navigation-with-typed-arguments">Navigation with typed arguments</a>
 */
@Parcelize
open class BaseArgs : Parcelable