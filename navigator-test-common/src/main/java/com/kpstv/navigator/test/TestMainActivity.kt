package com.kpstv.navigator.test

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.canFinish

class TestMainActivity : FragmentActivity() {
    lateinit var navigator: Navigator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigator = Navigator.with(this, savedInstanceState)
            .initialize(findViewById(R.id.my_container))
    }

    override fun onBackPressed() {
        if (navigator.canFinish())
            super.onBackPressed()
    }
}