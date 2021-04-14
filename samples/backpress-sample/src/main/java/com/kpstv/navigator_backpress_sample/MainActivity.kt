package com.kpstv.navigator_backpress_sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
        navigator = Navigator(supportFragmentManager, findViewById(R.id.container))
        navigator.navigateTo(
            Navigator.NavOptions(
                clazz = FirstFragment::class,
                args = AbstractWelcomeFragment.Args(
                    title = "First Fragment",
                    background = R.color.palette1,
                    nextColor = R.color.palette2,
                )
            )
        )
    }
    override fun onBackPressed() {
        if (navigator.canFinish())
            super.onBackPressed()
    }
}