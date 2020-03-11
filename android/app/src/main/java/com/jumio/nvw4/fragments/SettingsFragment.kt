package com.jumio.nvw4.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.jumio.nvw4.MainActivity
import com.jumio.nvw4.R
import kotlinx.android.synthetic.main.fragment_settings.*
import java.util.ArrayList

class SettingsFragment : Fragment() {

    companion object {
        var PERMISSION_REQUEST_CODE : Int = 1001

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

            var permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
            val missingPermissions = checkMissingPermissions(permissions)
            if (missingPermissions != null && missingPermissions.isNotEmpty()) {
                startPermissionRequest(missingPermissions)
            } else {
                (activity as MainActivity).showWebview(textinputedittext_url.text.toString())
            }
        }
    }


    private fun checkMissingPermissions(permissions: Array<String>): Array<String>? {
        val mp = ArrayList<String>()

        for (p in permissions) {
            if (activity?.let { ContextCompat.checkSelfPermission(it, p) } != PackageManager.PERMISSION_GRANTED) {
                mp.add(p)
            }
        }

        return if (mp.isEmpty()) null else mp.toTypedArray()
    }

    private fun startPermissionRequest(missingPermissions: Array<String>) {
        activity?.let { ActivityCompat.requestPermissions(it, missingPermissions,
            PERMISSION_REQUEST_CODE
        ) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity!!, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity!!, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}