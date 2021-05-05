package com.kpstv.bottom_navigation_sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.NavigatorTransmitter
import com.kpstv.navigation.canFinish

class MainActivity : AppCompatActivity(), NavigatorTransmitter {
    private lateinit var navigator: Navigator

    override fun getNavigator(): Navigator = navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            navigator = Navigator(supportFragmentManager, findViewById(R.id.container))
            navigator.navigateTo(MainFragment::class)
        }
    }

    override fun onBackPressed() {
        if (navigator.canFinish())
            super.onBackPressed()
    }
}