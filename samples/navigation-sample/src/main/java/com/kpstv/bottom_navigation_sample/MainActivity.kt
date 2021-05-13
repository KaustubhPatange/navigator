package com.kpstv.bottom_navigation_sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MenuItem
import androidx.core.view.GravityCompat
import com.kpstv.bottom_navigation_sample.databinding.ActivityMainBinding
import com.kpstv.navigation.Navigator
import com.kpstv.navigation.NavigatorTransmitter
import com.kpstv.navigation.canFinish

class MainActivity : AppCompatActivity(), NavigatorTransmitter {
    private lateinit var navigator: Navigator

    override fun getNavigator(): Navigator = navigator

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerFragmentLifecycleForLogging { frag, text ->
            Log.e(frag::class.simpleName, "=> $text")
        }

        navigator = Navigator.with(this, savedInstanceState).initialize(findViewById(R.id.container))
        if (savedInstanceState == null) {
            navigator.navigateTo(MainFragment::class)
        }

        setToolbar()
    }

    private fun setToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            binding.root.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (binding.root.isDrawerOpen(GravityCompat.START)) {
            binding.root.closeDrawer(GravityCompat.START)
            return
        }
        if (navigator.canFinish())
            super.onBackPressed()
    }
}