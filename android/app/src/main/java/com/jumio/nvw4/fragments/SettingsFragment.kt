package com.jumio.nvw4.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.jumio.nvw4.MainActivity
import com.jumio.nvw4.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    companion object {
        var PERMISSION_REQUEST_CODE: Int = 1001

        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }

        private var _binding: FragmentSettingsBinding? = null
        private val binding get() = _binding
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding?.buttonStart?.setOnClickListener {
            val permissions =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.RECORD_AUDIO,
                    )
                } else {
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                    )
                }
            val missingPermissions = checkMissingPermissions(permissions)
            if (!missingPermissions.isNullOrEmpty()) {
                startPermissionRequest(missingPermissions)
            } else {
                activity?.window?.setFlags(
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                )

                (activity as MainActivity).showWebview(binding?.textinputedittextUrl?.text.toString().trim())
            }
        }
    }

    private fun checkMissingPermissions(permissions: Array<String>): Array<String>? {
        val mp = ArrayList<String>()

        for (p in permissions) {
            if (activity?.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        p,
                    )
                } != PackageManager.PERMISSION_GRANTED
            ) {
                mp.add(p)
            }
        }

        return if (mp.isEmpty()) null else mp.toTypedArray()
    }

    private fun startPermissionRequest(missingPermissions: Array<String>) {
        activity?.let {
            ActivityCompat.requestPermissions(
                it,
                missingPermissions,
                PERMISSION_REQUEST_CODE,
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireActivity(), "Permission Granted", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireActivity(), "Permission Denied", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }
}
