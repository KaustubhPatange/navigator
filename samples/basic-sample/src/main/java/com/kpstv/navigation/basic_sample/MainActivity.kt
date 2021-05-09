package com.kpstv.navigation.basic_sample

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kpstv.navigation.*
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity(), NavigatorTransmitter {
    private lateinit var navigator: Navigator
    private val viewModel by viewModels<MainViewModel>()

    override fun getNavigator(): Navigator = navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigator = Navigator.with(this, savedInstanceState).initialize(findViewById(R.id.container))

        viewModel.navigation.observe(this) { option ->
            navigator.navigateTo(option.clazz, option.options)
        }

       if (savedInstanceState == null) {
           viewModel.navigate(screen = Screens.MAIN)
       }
    }

    enum class Screens(val clazz: KClass<out Fragment>) {
        MAIN(MainFragment::class),
        FIRST(FragFirst::class),
        SECOND(FragSecond::class)
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
        val options = Navigator.NavOptions(
            args = args,
            animation = animation,
            remember = remember
        )
        navigation.value = NavigationOptions(screen.clazz, options)
    }

    class NavigationOptions(
        val clazz: KClass<out Fragment>,
        val options: Navigator.NavOptions
    )
}