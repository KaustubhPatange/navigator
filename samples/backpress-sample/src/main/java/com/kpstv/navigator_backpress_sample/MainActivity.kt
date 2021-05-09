package com.kpstv.navigator_backpress_sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.NavigatorTransmitter
import com.kpstv.navigation.canFinish

class MainActivity : AppCompatActivity(), NavigatorTransmitter {
    private lateinit var navigator: Navigator
    override fun getNavigator(): Navigator = navigator
    override fun onCreate(savedInstanceState: Bundle?) {
        makeFullScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerFragmentLifecycleForLogging { frag, log ->
            Log.e(frag::class.simpleName, "=> $log")
        }

        navigator = Navigator.with(this, savedInstanceState).initialize(findViewById(R.id.container))

        if (savedInstanceState == null) {
            val options = Navigator.NavOptions(
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