package com.kpstv.navigation.internals

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.animation.addListener
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.kpstv.navigation.AnimationDefinition
import com.kpstv.navigation.R
import kotlin.math.hypot

internal class NavigatorCircularTransform(
    private val getCurrentHistory: () -> List<BackStackRecord>,
    private val fm: FragmentManager,
    private val fragmentContainer: FrameLayout
) {
    private val circularTransformStack = mutableMapOf<String, AnimationDefinition.CircularReveal>()
    private val containerView = fragmentContainer.rootView as FrameLayout

    init {
        fm.addOnBackStackChangedListener {
            // update
            val keys = circularTransformStack.keys.toList()
            val current = getCurrentHistory.invoke().map { it.name }
            keys.subtract(current).forEach { circularTransformStack.remove(it) }
        }
    }

    fun executeReverseTransform() : Boolean {
        if (circularTransformStack.isNotEmpty()) {
            val current = fm.findFragmentById(fragmentContainer.id)?.tag
            val lastKey = circularTransformStack.keys.last()
            if (lastKey == current) {
                val payload = circularTransformStack[lastKey] ?: return false
                circularTransform(AnimationDefinition.CircularReveal(fromTarget = payload.fromTarget), popUpTo = false, reverse = true, currentTag = null)
            }
        }
        return false
    }

    fun circularTransform(payload: AnimationDefinition.CircularReveal, currentTag: String?, popUpTo: Boolean = false) {
        circularTransform(payload = payload, popUpTo = popUpTo, currentTag = currentTag, reverse = false)
    }

    private fun circularTransform(payload: AnimationDefinition.CircularReveal, popUpTo: Boolean, currentTag: String?, reverse: Boolean) {
        if (fragmentContainer.childCount <= 0) return
        if (!containerView.isLaidOut) return

        val viewBitmap = containerView.drawToBitmap()
        val overlayView = createEmptyImageView().apply {
            setImageBitmap(viewBitmap)
            setTag(R.id.fragment_container_view_tag, Fragment()) // For fragment container
        }

        if (!reverse) {
            if (!popUpTo && currentTag != null) {
                circularTransformStack[currentTag] = payload
            } else if (popUpTo) {
                circularTransformStack.clear()
                containerView.addView(overlayView)
            }
            fm.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentViewCreated(
                    fm: FragmentManager,
                    f: Fragment,
                    v: View,
                    savedInstanceState: Bundle?
                ) {
                    super.onFragmentViewCreated(fm, f, v, savedInstanceState)
                    fm.unregisterFragmentLifecycleCallbacks(this)

                    if (!popUpTo) containerView.addView(overlayView)

                    val target = payload.fromTarget ?: Rect(0, 0, containerView.width, containerView.height)

                    if (!f.isRemoving) {
                        if (f.requireView().isLaidOut) {
                            f.childFragmentManager.executePendingTransactions()
                            performRestCircularTransform(overlayView, target)
                        } else {
                            f.requireView().doOnLaidOut {
                                f.childFragmentManager.executePendingTransactions()
                                performRestCircularTransform(overlayView, target)
                            }
                        }
                    } else containerView.removeView(overlayView)
                }
            }, false)
        } else {
            circularTransformStack.keys.lastOrNull()?.let { circularTransformStack.remove(it) }

            containerView.addView(overlayView)

            fm.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                    super.onFragmentViewDestroyed(fm, f)
                    fm.unregisterFragmentLifecycleCallbacks(this)

                    val target = payload.fromTarget ?: Rect(0, 0, containerView.width, containerView.height)
                    performReverseCircularTransform(overlayView, target)
                }
            }, false)
        }
    }

    fun onSaveStateInstance(outBundle: Bundle) {
        if (circularTransformStack.isNotEmpty()) {
            outBundle.putStringArrayList(getSaveableMapKey(), ArrayList(circularTransformStack.keys))
            outBundle.putParcelableArrayList(getSaveableMapValue(), ArrayList(circularTransformStack.values))
        }
    }

    fun restoreStateInstance(bundle: Bundle?) {
        if (bundle != null) {
            val list = bundle.getStringArrayList(getSaveableMapKey()) ?: return
            val values = bundle.getParcelableArrayList<AnimationDefinition.CircularReveal>(getSaveableMapValue()) ?: return
            val entries = list.zip(values).associate { it.first to it.second }
            circularTransformStack.putAll(entries)
        }
    }

    private fun performRestCircularTransform(overlayView: ImageView, target: Rect) {
        val overlayView2 = createEmptyImageView().apply {
            setTag(R.id.fragment_container_view_tag, Fragment()) // For fragment container
            containerView.addView(this)
        }

        val anim = ViewAnimationUtils.createCircularReveal(
            overlayView2,
            target.centerX(),
            target.centerY(),
            0f,
            hypot(containerView.width.toFloat(), containerView.height.toFloat())
        ).apply {
            addListener(
                onStart = {
                    overlayView.visibility = View.INVISIBLE
                    val secondBitmap = containerView.drawToBitmap()
                    overlayView2.setImageBitmap(secondBitmap)
                    overlayView.visibility = View.VISIBLE
                },
                onEnd = {
                    containerView.removeView(overlayView)
                    containerView.removeView(overlayView2)
                }
            )
            duration = 400
        }

        anim.start()
    }

    private fun performReverseCircularTransform(overlayView: ImageView, target: Rect) {
        val anim = ViewAnimationUtils.createCircularReveal(
            overlayView,
            target.centerX(),
            target.centerY(),
            hypot(containerView.width.toFloat(), containerView.height.toFloat()),
            0f,
        ).apply {
            addListener(onEnd = {
                containerView.removeView(overlayView)
            })
            duration = 400
        }

        anim.start()
    }

    private fun createEmptyImageView() : ImageView {
        return ImageView(containerView.context).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
    }

    private fun getSaveableMapKey(): String = "$SAVED_STATE_KEY${containerView.id}_key"
    private fun getSaveableMapValue(): String = "$SAVED_STATE_KEY${containerView.id}_value"

    companion object {
        private const val SAVED_STATE_KEY = "com.kpstv.navigator:circularTransform:"
    }
}