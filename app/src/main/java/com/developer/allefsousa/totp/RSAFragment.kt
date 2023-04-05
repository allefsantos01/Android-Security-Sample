package com.developer.allefsousa.totp

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.developer.allefsousa.totop.R
import com.developer.allefsousa.totop.databinding.FragmentRsaBinding
import com.developer.allefsousa.totp.cryptography.RsaKeystoreWrapper
import java.security.interfaces.RSAPublicKey


class RSAFragment : Fragment(R.layout.fragment_rsa) {
    private var _binding: FragmentRsaBinding? = null
    private val binding get() = _binding
    private var control = ControlEnum.GENERATE
    private val rsaWrapper = RsaKeystoreWrapper()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRsaBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.btnGenerate?.setOnClickListener {
            when(control){
                ControlEnum.GENERATE -> generateKeyPair()
                ControlEnum.RECOVERY -> recoveKeyPair()
                ControlEnum.ENCRYPT -> encrypt()
                ControlEnum.DECRYPT -> decrypt()
            }

        }
        binding?.chipGroup?.setOnCheckedStateChangeListener { group, checkedIds ->
            Log.d("Allef", "onViewCreated: ${group.checkedChipId}")
               when(group.checkedChipId){
                   R.id.chipGenerated->{
                       binding?.btnGenerate?.text = "Gerar Chave"
                       control = ControlEnum.GENERATE
                   }
                   R.id.chipRecovery ->{
                       binding?.btnGenerate?.text = "Recuperar Chave"
                       control = ControlEnum.RECOVERY
                   }
               }
        }
        binding?.chipGroup2?.setOnCheckedStateChangeListener { group, checkedIds ->
            Log.d("Allef", "onViewCreated: ${group.checkedChipId}")
               when(group.checkedChipId){
                   R.id.encrypt->{
                       binding?.btnGenerate?.text = "Criptografar informação"
                       control = ControlEnum.ENCRYPT
                   }
                   R.id.decrypt ->{
                       binding?.btnGenerate?.text = "Descriptografar informação"
                       control = ControlEnum.DECRYPT
                   }
               }
        }

    }

    private fun decrypt() {
        rsaWrapper.decrypt(binding?.alias2?.text.toString(),binding?.alias?.text.toString())
    }

    private fun encrypt() {
        rsaWrapper.encrypt(binding?.alias2?.text.toString(),binding?.alias?.text.toString())
    }

    private fun generateKeyPair() {
        val pair = rsaWrapper.createAsymmetricKeyPair(binding?.alias?.text.toString())
        Log.d(RSAFragment::class.java.name, "Chave publica: ${pair?.public}")

        //val aa = rsaWrapper.recoveryPublic(binding?.alias.toString()) as RSAPublicKey
        //rsaWrapper.savePrivateKey(pair,binding?.alias?.text.toString())

        val stringBuilder = StringBuilder()
        stringBuilder.append("Key Alias = ${binding?.alias?.text.toString()}\n\n\n Chave Privada = ${pair?.private}  \n\n\nChave Publica = ${pair?.public}")
        binding?.result?.text = stringBuilder

        Log.d(RSAFragment::class.java.name, "Chave privada: ${pair?.private}")
        Log.d(RSAFragment::class.java.name, "Chave privada: ${pair?.public}")
        Log.d(RSAFragment::class.java.name, "Chave privada: ${pair?.public?.encoded}")
       // Log.d(RSAFragment::class.java.name, "Chave Publica AA: ${aa.modulus}")
        //Log.d(RSAFragment::class.java.name, "Chave Publica AA: ${aa.publicExponent}")
       // Log.d(RSAFragment::class.java.name, "Chave Publica AA: ${Base64.encodeToString(aa.encoded,Base64.DEFAULT)}")
        Log.d(RSAFragment::class.java.name, "Chave Publica AA: ${Base64.encodeToString(pair?.public?.encoded,Base64.DEFAULT)}")
    }

    private fun recoveKeyPair() {
        val rsaWrapper = RsaKeystoreWrapper()
        val pair2 = rsaWrapper.recoveryAsymmetricKeyPair(binding?.alias?.text.toString())
        Log.d(RSAFragment::class.java.name, "Chave publica: ${pair2?.public}")
        val stringBuilder = StringBuilder()
        stringBuilder.append("Key= ${binding?.alias?.text.toString()}\n\n\n Chave Privada = ${pair2?.private}  \n\n\nChave Publica = ${pair2?.public}")
        binding?.result?.text = stringBuilder

        Log.d(RSAFragment::class.java.name, "Chave publica: ${pair2?.public}")
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = RSAFragment()
    }
}