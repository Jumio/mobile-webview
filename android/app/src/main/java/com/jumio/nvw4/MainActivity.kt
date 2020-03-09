package com.jumio.nvw4

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import com.jumio.nvw4.fragments.SettingsFragment
import com.jumio.nvw4.fragments.WebviewFragment

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (savedInstanceState == null) {
            supportFragmentManager.
                beginTransaction().
                add(R.id.fragment_container, SettingsFragment.newInstance()).
                commit()
        }
    }

    fun showWebview(url:String) {
        supportFragmentManager.
            beginTransaction().
            replace(R.id.fragment_container, WebviewFragment.newInstance(url)).
            addToBackStack("webview").
            commitAllowingStateLoss()
    }

}
