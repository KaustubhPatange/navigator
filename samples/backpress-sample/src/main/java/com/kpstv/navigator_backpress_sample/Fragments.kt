package com.kpstv.navigator_backpress_sample

import android.content.res.ColorStateList
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment
import com.kpstv.navigation.*
import com.kpstv.navigator_backpress_sample.databinding.FragmentDisclaimerBinding
import kotlinx.parcelize.Parcelize
import kotlin.reflect.KClass

abstract class AbstractWelcomeFragment : ValueFragment(R.layout.fragment_disclaimer) {
    private var viewBinding: FragmentDisclaimerBinding? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentDisclaimerBinding.bind(view).also { viewBinding = it }
        val args = getKeyArgs<Args>()
        binding.tvTitle.text = args.title
        binding.btnNext.applyBottomInsets()
        binding.btnNext.text = if (args.hasNext) "Next" else "Finish"
        binding.root.background = ColorDrawable(colorFrom(args.background))

        if (getNextFragment() == null) {
            binding.btnNext.visibility = View.GONE
        }

        binding.btnNext.setTextColor(colorFrom(args.background))
        binding.btnNext.backgroundTintList = ColorStateList.valueOf(colorFrom(args.nextColor))
        binding.btnNext.setOnClickListener click@{
            val nextFragment = getNextFragment() ?: return@click

            if (!args.hasNext) {
                getParentNavigator().navigateTo(
                    Navigator.NavOptions(
                        clazz = nextFragment,
                        args = getNextArgs(),
                        animation = AnimationDefinition.Fade(),
                        popUpToThis = true
                    )
                )
            } else {
                val rect = Rect()
                binding.btnNext.getGlobalVisibleRect(rect)

                getParentNavigator().navigateTo(
                    Navigator.NavOptions(
                        clazz = nextFragment,
                        args = getNextArgs(),
                        animation = AnimationDefinition.CircularReveal(getNextFragment(), rect),
                        addToBackStack = true
                    )
                )
            }
        }
    }

    override fun onDestroyView() {
        viewBinding = null
        super.onDestroyView()
    }

    abstract fun getNextArgs(): Args?
    abstract fun getNextFragment(): KClass<out Fragment>?

    @Parcelize
    data class Args(val title: String, @ColorRes val background: Int, @ColorRes val nextColor: Int, val hasNext: Boolean = true) : BaseArgs(), Parcelable
}

class FirstFragment : AbstractWelcomeFragment() {
    override fun getNextArgs(): Args = Args(
        title = "Second Fragment",
        background = R.color.palette2,
        nextColor = R.color.palette3
    )
    override fun getNextFragment(): KClass<out Fragment> = SecondFragment::class
}

class SecondFragment : AbstractWelcomeFragment() {
    override fun getNextArgs(): Args = Args(
        title = "Third Fragment",
        background = R.color.palette3,
        nextColor = R.color.palette4,
        hasNext = false
    )
    override fun getNextFragment(): KClass<out Fragment> = ThirdFragment::class
}

class ThirdFragment : AbstractWelcomeFragment() {
    override fun getNextArgs(): Args = Args(
        title = "Last Fragment",
        background = R.color.palette4,
        nextColor = R.color.palette5,
        hasNext = false
    )
    override fun getNextFragment(): KClass<out Fragment> = LastFragment::class
}

class LastFragment : AbstractWelcomeFragment() {
    override fun getNextArgs(): Args? = null
    override fun getNextFragment(): KClass<out Fragment>? = null
}