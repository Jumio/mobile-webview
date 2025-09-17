package com.jumio.nvw4.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jumio.nvw4.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
	companion object {
		fun newInstance(): SettingsFragment {
			return SettingsFragment()
		}

		private var _binding: FragmentSettingsBinding? = null
		private val binding get() = _binding
	}

	private val callback: FragmentCallback?
		get() = context as? FragmentCallback

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
		binding?.apply {
			buttonStart.apply {
				isEnabled = false
				setOnClickListener {
					callback?.showWebViewOrRequestPermission(binding?.textinputedittextUrl?.text.toString().trim())
				}
			}
			textinputedittextUrl.addTextChangedListener(object : TextWatcher {
				override fun afterTextChanged(s: Editable?) {
					binding?.buttonStart?.isEnabled = !s.isNullOrBlank()
				}

				override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
				override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
			})
		}
	}
}
