package com.kpstv.navigator_backpress_sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kpstv.navigation.Destination
import com.kpstv.navigation.FragmentNavigator
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

        val args = AbstractWelcomeFragment.Args(
            title = "First Fragment",
            background = R.color.palette1,
            nextColor = R.color.palette2,
        )
        navigator = FragmentNavigator.with(this, savedInstanceState)
            .initialize(findViewById(R.id.container), Destination.of(FirstFragment::class to args))
    }
    
    override fun onBackPressed() {
        if (navigator.canFinish())
            super.onBackPressed()
    }
}