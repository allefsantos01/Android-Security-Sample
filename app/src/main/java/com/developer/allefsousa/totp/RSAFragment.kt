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
import com.developer.allefsousa.totp.cryptography.hasMarshmallow
import java.security.KeyPair
import java.security.interfaces.RSAPublicKey


class RSAFragment : Fragment(R.layout.fragment_rsa) {
    private var _binding: FragmentRsaBinding? = null
    private val binding get() = _binding
    private var control = ControlEnum.GENERATE
    private val rsaWrapper = RsaKeystoreWrapper()
    private val resultCripto = StringBuilder()

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
                ControlEnum.GENERATE -> {
                    generateKeyPair()
                    binding?.group?.visibility = View.VISIBLE
                    binding?.chipGroup2?.check(R.id.encrypt)
                }
                ControlEnum.RECOVERY -> {
                    binding?.result2?.text = ""
                    binding?.result?.text = ""
                    recoveKeyPair()
                }
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
        val decripted = if (hasMarshmallow()){
            rsaWrapper.decrypt(binding?.alias2?.text.toString(),binding?.alias?.text.toString())
        }else{
            rsaWrapper.decrypt(binding?.alias2?.text.toString(),rsaWrapper.storeKeyPair.private)
        }

        resultCripto.appendLine("-------------------------------------------------------------------------------")
        resultCripto.appendLine("")
        resultCripto.appendLine("Valor descriptografado \n$decripted")
        resultCripto.appendLine("")
        resultCripto.appendLine("-------------------------------------------------------------------------------")

        binding?.result2?.text = resultCripto

    }

    private fun encrypt() {
        //val resulSing = rsaWrapper.signData(binding?.alias2?.text.toString(),binding?.alias?.text.toString())
        val resultEncrypted  =if (hasMarshmallow()){
            rsaWrapper.encrypt(binding?.alias2?.text.toString(),binding?.alias?.text.toString())
        }else{
            rsaWrapper.encrypt(binding?.alias2?.text.toString(),rsaWrapper.storeKeyPair.public)
        }
        resultCripto.appendLine("-------------------------------------------------------------------------------")
        resultCripto.appendLine("")
        resultCripto.appendLine("Valor Criptografado com a chave publica \n${resultEncrypted}")
        resultCripto.appendLine("")
        resultCripto.appendLine("-------------------------------------------------------------------------------")
        binding?.alias2?.text?.clear()
        binding?.alias2?.setText(resultEncrypted)

    }

    private fun generateKeyPair() {
        val pair = rsaWrapper.createAsymmetricKeyPair(binding?.alias?.text.toString())
        Log.d(RSAFragment::class.java.name, "Chave publica: ${pair?.public}")

        val stringBuilder = StringBuilder()
        stringBuilder.append("Key Alias = ${binding?.alias?.text.toString()}\n\n\n Chave Privada = ${pair?.private}  \n\n\nChave Publica = ${pair?.public}")
        binding?.result?.text = stringBuilder
        resultCripto.appendLine("-------------------------------------------------------------------------------")
        resultCripto.appendLine("")
        resultCripto.appendLine("Chave Publica \n${Base64.encodeToString(pair.public?.encoded, Base64.DEFAULT)}")
        resultCripto.appendLine("")
        resultCripto.appendLine("-------------------------------------------------------------------------------")
        Log.d(RSAFragment::class.java.name, "Chave Publica AA: ${Base64.encodeToString(pair?.public?.encoded,Base64.DEFAULT)}")
    }

    private fun recoveKeyPair() {
        val rsaWrapper = RsaKeystoreWrapper()
        val pair2 = rsaWrapper.recoveryAsymmetricKeyPair(binding?.alias?.text.toString())

        pair2?.let {
             resultCripto.appendLine("-------------------------------------------------------------------------------")
            resultCripto.appendLine("")
            resultCripto.appendLine("Chave Publica \n${Base64.encodeToString(it.public?.encoded, Base64.DEFAULT)}")
            resultCripto.appendLine("")
            resultCripto.appendLine("-----------------------------------------------------------------------------")
             val stringBuilder = StringBuilder()
             stringBuilder.append("Key= ${binding?.alias?.text.toString()}\n\n\n Chave Privada = ${it.private}  \n\n\nChave Publica = ${it.public}")
             binding?.result?.text = stringBuilder
            Log.d(RSAFragment::class.java.name, "Chave publica: ${it.public}")

            binding?.group?.visibility = View.VISIBLE
            binding?.chipGroup2?.check(R.id.encrypt)

        } ?: change()

    }

    private fun change() {
        binding?.result?.text = "Chave não cadastrada!"
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