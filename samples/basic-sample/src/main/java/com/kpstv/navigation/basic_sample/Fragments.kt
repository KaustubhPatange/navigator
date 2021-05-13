package com.kpstv.navigation.basic_sample

import android.os.Bundle
import android.os.Parcelable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.kpstv.navigation.AnimationDefinition
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.ValueFragment
import kotlinx.parcelize.Parcelize

class MainFragment : ValueFragment() {
    private val viewModel by viewModels<MainViewModel>(::requireActivity)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            addView(
                Button(context).apply {
                    text = "Go to First Fragment"
                    setOnClickListener {
                        viewModel.navigate(
                            screen = MainActivity.Screens.FIRST,
                            animation = AnimationDefinition.Custom(
                                destinationEntering = R.transition.myfade,
                                currentExiting = R.transition.myfade
                            ),
                            args = AbstractArgs("First Fragment"),
                            remember = true
                        )
                    }
                }
            )
            addView(
                Button(context).apply {
                    text = "Go to Second Fragment"
                    setOnClickListener {
                        viewModel.navigate(
                            screen = MainActivity.Screens.SECOND,
                            animation = AnimationDefinition.SlideInRight,
                            args = AbstractArgs("Second Fragment"),
                            remember = true
                        )
                    }
                }
            )
        }
    }
}

@Parcelize
data class AbstractArgs(val title: String) : BaseArgs(), Parcelable

open class AbstractTextFragment : ValueFragment(R.layout.fragment_common) {
    private val textView: TextView by lazy { requireView().findViewById(R.id.textView) }
    private val toolbar: Toolbar by lazy { requireView().findViewById(R.id.toolbar) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (hasKeyArgs<AbstractArgs>()) {
            val args = getKeyArgs<AbstractArgs>()
            textView.text = args.title
            toolbar.title = args.title
        }

        toolbar.setNavigationOnClickListener { goBack() }
    }
}

class FragFirst : AbstractTextFragment()
class FragSecond : AbstractTextFragment()