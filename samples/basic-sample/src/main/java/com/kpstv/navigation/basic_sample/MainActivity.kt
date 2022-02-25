package com.kpstv.navigation.basic_sample

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kpstv.navigation.*
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity(), FragmentNavigator.Transmitter {
    private lateinit var navigator: FragmentNavigator
    private val viewModel by viewModels<MainViewModel>()

    override fun getNavigator(): FragmentNavigator = navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigator = FragmentNavigator.with(this, savedInstanceState)
            .initialize(findViewById(R.id.container), Destination.of(MainFragment::class))

        viewModel.navigation.observe(this) { option ->
            if (!option.isConsumed()) {
                navigator.navigateTo(option.clazz, option.options)
                option.consumed()
            }
        }
    }

    enum class Screens(val clazz: KClass<out Fragment>) {
        MAIN(MainFragment::class),
        FIRST(FragFirst::class),
        SECOND(FragSecond::class)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        println("")
    }

    override fun onBackPressed() {
        if (navigator.canFinish())
            super.onBackPressed()
    }
}

class MainViewModel : ViewModel() {
    internal val navigation = MutableLiveData<NavigationOptions>()

    fun navigate(
        screen: MainActivity.Screens,
        args: BaseArgs? = null,
        animation: NavAnimation = AnimationDefinition.None,
        remember: Boolean = false
    ) {
        val options = FragmentNavigator.NavOptions(
            args = args,
            animation = animation,
            remember = remember
        )
        navigation.value = NavigationOptions(screen.clazz, options)
    }

    class NavigationOptions(
        val clazz: KClass<out Fragment>,
        val options: FragmentNavigator.NavOptions,
    ) {
        private var consumed: Boolean = false
        fun consumed() {
            consumed = true
        }
        fun isConsumed() = consumed
    }
}