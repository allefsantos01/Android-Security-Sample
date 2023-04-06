package com.developer.allefsousa.totp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.developer.allefsousa.totop.databinding.FragmentReceiveKeyBinding
import com.developer.allefsousa.totop.databinding.FragmentRsaBinding
import com.developer.allefsousa.totp.cryptography.RsaKeystoreWrapper

class ReceiveRSAServerFragment :
    Fragment(com.developer.allefsousa.totop.R.layout.fragment_receive_key) {

    private var _binding: FragmentReceiveKeyBinding? = null
    private val binding get() = _binding
    private val rsaWrapper = RsaKeystoreWrapper()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReceiveKeyBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.btnGenerate?.setOnClickListener {

            val publicKey =
                if (binding?.alias?.text?.isNotEmpty() == true) binding?.alias?.text.toString()
                else "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0dDAyyK1dp0VVowV45HYLtO/1BtrW4+y5aO5LqxZTRLok/Q2w9OdQ/ufDSe0IIX1EkyZejNBr3pcm9h8GyBje/FpqDxNjgymKfPLIQabMbhoxVqg1kZMIsS1/gUiOVP4Sqfhvvb0W6T3nU1qpevHQ5r2+LbinRO3GcvT3w0On+8jMoKEANVpZTJrndmNxxoa00KEB4nUieMcQ68UT2UG6sWyIP/kd1LsR70uMb2qaFI51U1OH/cX/HYWPeNFucuxRBJF6KLxgGsEr3lPofgvfiDbZ/kzGl2BcpLawZy23YbToOeEl3sUr2cb+UJkkqytRBfMbqYz9Lju/1r1t1mwKQIDAQAB"

            val texto = rsaWrapper.saveServerPublicKey(
                binding?.texto2?.text.toString(),
                publicKey
            )
            binding?.textResult?.text = texto

            Log.d(ReceiveRSAServerFragment::class.simpleName, "Result: $texto")
        }

    }

    companion object {
        @JvmStatic
        fun newInstance() = ReceiveRSAServerFragment()
    }
}