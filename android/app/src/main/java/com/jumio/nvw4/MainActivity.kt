package com.jumio.nvw4

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jumio.nvw4.databinding.ActivityMainBinding
import com.jumio.nvw4.fragments.SettingsFragment
import com.jumio.nvw4.fragments.WebViewFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        val toolbar = binding.toolbar

        setContentView(view)
        setSupportActionBar(toolbar)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, SettingsFragment.newInstance())
                .commit()
        }
    }

    fun showWebView(url: String) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, WebViewFragment.newInstance(url))
            .addToBackStack("webview").commitAllowingStateLoss()
    }
}
