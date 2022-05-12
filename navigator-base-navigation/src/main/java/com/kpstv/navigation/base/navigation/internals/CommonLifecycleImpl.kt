package com.kpstv.navigation.base.navigation.internals

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.kpstv.navigation.ValueFragment

@RestrictTo(RestrictTo.Scope.LIBRARY)
class FragmentNavigationLifecycle(
    private val fragment: Fragment,
    private val impl: CommonNavigationImpl,
    private val onSaveInstance: (ValueFragment, Bundle) -> Unit
) : FragmentManager.FragmentLifecycleCallbacks() {

    override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
        if (fragment === f) {
            impl.onSaveInstanceState(outState)
        }
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        if (fragment === f) {
            // If the view is destroyed but fragment did not then it's likely that we do not get
            // callback on SaveInstanceState in such case we will save it in the fragment bundle.
            if (f is ValueFragment) {
                val bundle = Bundle()
                impl.onSaveInstanceState(bundle)
                onSaveInstance(f, bundle) // let each implementation manage the save state.
            }
        }
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        if (fragment === f) {
            fm.unregisterFragmentLifecycleCallbacks(this)
        }
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY)
class ActivityNavigationLifecycle(
    private val activity: FragmentActivity,
    private val impl: CommonNavigationImpl
) : Application.ActivityLifecycleCallbacks {
    override fun onActivitySaveInstanceState(a: Activity, outState: Bundle) {
        if (activity::class == a::class) {
            impl.onSaveInstanceState(outState)
        }
    }

    override fun onActivityDestroyed(a: Activity) {
        if (activity::class == a::class) {
            activity.application.unregisterActivityLifecycleCallbacks(this)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
}