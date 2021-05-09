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

        navigator = Navigator.with(this, savedInstanceState).initialize(findViewById(R.id.container))
        if (savedInstanceState == null) {
            navigator.navigateTo(MainFragment::class)
        }
    }

    override fun onBackPressed() {
        if (navigator.canFinish())
            super.onBackPressed()
    }
}