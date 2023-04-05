package com.developer.allefsousa.totp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import com.developer.allefsousa.totop.databinding.ActivityGeneratorBinding

private const val SECRET = "TTDPFTLZE7BZBL7PEOGWEAK3IH7ASXUS"
class GeneratorFragment : Fragment(com.developer.allefsousa.totop.R.layout.activity_generator) {
    private var _binding: ActivityGeneratorBinding? = null
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityGeneratorBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val authenticator = TotpAuthenticator(SECRET.toByteArray())
        setupListeners(authenticator)
    }

    private fun setupListeners(authenticator: TotpAuthenticator) {
        binding?.btnGenerate?.setOnClickListener {
            generated(authenticator)
        }

        binding?.secret?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                generated(authenticator)
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun generated(authenticator: TotpAuthenticator) = with(binding) {
        if (this!!.secret.text!!.isEmpty()) {
            textSecret.text = "Secret: ${SECRET}"
            textCode.text = "TOTP Code: ${authenticator.generate()}"
        } else {
            val newAuth = TotpAuthenticator(secret.text.toString().toByteArray())
            textSecret.text = "Secret: ${binding!!.secret.text.toString()}"
            textCode.text = "TOTP Code: ${newAuth.generate()}"
            secret.text?.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = GeneratorFragment()
    }

}