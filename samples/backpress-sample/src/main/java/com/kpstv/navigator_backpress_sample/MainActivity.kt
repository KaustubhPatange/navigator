package com.kpstv.navigator_backpress_sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.canFinish

class MainActivity : AppCompatActivity(), FragmentNavigator.Transmitter {
    private lateinit var navigator: FragmentNavigator
    override fun getNavigator(): FragmentNavigator = navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        makeFullScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerFragmentLifecycleForLogging { frag, log ->
            Log.e(frag::class.simpleName, "=> $log")
        }

        navigator = Navigator.with(this, savedInstanceState)
            .setNavigator(FragmentNavigator::class)
            .initialize(findViewById(R.id.container))

        if (savedInstanceState == null) {
            val options = FragmentNavigator.NavOptions(
                args = AbstractWelcomeFragment.Args(
                    title = "First Fragment",
                    background = R.color.palette1,
                    nextColor = R.color.palette2,
                )
            )
            navigator.navigateTo(FirstFragment::class, options)
        }
    }
    override fun onBackPressed() {
        if (navigator.canFinish())
            super.onBackPressed()
    }
}