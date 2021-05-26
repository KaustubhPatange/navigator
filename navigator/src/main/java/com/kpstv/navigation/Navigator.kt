package com.kpstv.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlin.reflect.KClass

interface Navigator {
    fun canGoBack(): Boolean
    fun goBack(): Boolean

    class Builder internal constructor(private val owner: Any, private val savedInstanceState: Bundle?) {
        fun setNavigator(fragNavClass: KClass<FragmentNavigator>) : FragmentNavigator.Builder {
            return when(owner) {
                is FragmentActivity -> FragmentNavigator.Builder(owner.supportFragmentManager, savedInstanceState).apply { set(owner) }
                is Fragment -> FragmentNavigator.Builder(owner.childFragmentManager, savedInstanceState).apply { set(owner, owner.parentFragmentManager) }
                else -> throw IllegalArgumentException("No constructor matching $owner is found.")
            }
        }

        // TODO: Add support for Jetpack Compose
    }

    companion object {
        /**
         * Returns a builder for creating an instance of Navigator.
         */
        fun with(activity: FragmentActivity, savedInstanceState: Bundle?): Builder {
            return Builder(activity, savedInstanceState)
        }

        /**
         * Returns a builder for creating an instance of Navigator.
         */
        fun with(fragment: Fragment, savedInstanceState: Bundle?): Builder {
            return Builder(fragment, savedInstanceState)
        }
    }
}
