package com.jumio.nvw4.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jumio.nvw4.MainActivity
import com.jumio.nvw4.R
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment() {

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_start.setOnClickListener {
            (activity as MainActivity).showWebview(textinputedittext_url.text.toString())
        }
    }

}