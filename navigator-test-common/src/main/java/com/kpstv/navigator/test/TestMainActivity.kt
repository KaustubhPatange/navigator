package com.kpstv.navigator.test

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.Destination
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.canFinish

class TestMainActivity : FragmentActivity(), FragmentNavigator.Transmitter {
    private lateinit var navigator: FragmentNavigator

    override fun getNavigator(): FragmentNavigator = navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigator = Navigator.with(this, savedInstanceState)
            .setNavigator(FragmentNavigator::class)
            .initialize(findViewById(R.id.my_container))
    }

    override fun onBackPressed() {
        if (navigator.canFinish())
            super.onBackPressed()
    }
}