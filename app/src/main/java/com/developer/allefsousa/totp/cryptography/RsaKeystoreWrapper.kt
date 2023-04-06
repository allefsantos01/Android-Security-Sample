package com.developer.allefsousa.totp.cryptography

import android.annotation.TargetApi
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.security.keystore.UserNotAuthenticatedException
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.ByteArrayInputStream
import java.security.*
import java.security.KeyStore.TrustedCertificateEntry
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher


class RsaKeystoreWrapper {
    companion object {
        const val RSA_TRANS = "RSA/ECB/PKCS1Padding"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "keyPP"
    }

    private var signatureResult: String = ""
     var storeKeyPair = KeyPair(null,null)


    private fun createKeyStore(): KeyStore {
        val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore
    }

    fun createAsymmetricKeyPair(alias: String): KeyPair {
        val generator: KeyPairGenerator

         return if (hasMarshmallow()) {
            generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)
            getKeyGenParameterSpec(generator, alias)
            generator.generateKeyPair()

        } else {
            generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
            generator.initialize(2048)
            val keypar = generator.generateKeyPair()
             storeKeyPair = keypar
             Log.d(RsaKeystoreWrapper::class.simpleName, "Publica: ${keypar.public}")
             Log.d(RsaKeystoreWrapper::class.simpleName, "Publica Encoded: ${Base64.encodeToString(keypar.public.encoded,Base64.DEFAULT)}")
             Log.d(RsaKeystoreWrapper::class.simpleName, "Privada: ${keypar.private}")
             Log.d(RsaKeystoreWrapper::class.simpleName, "Private encoded: ${Base64.encodeToString(keypar.private.encoded,Base64.DEFAULT)}")

             // salvar no keystore e recuperar
             //saveKeyOldVersion(keypar,alias)
             keypar
         }

    }

    // TODO: A chave PUblica do backend vai ser salva no armazenamento interno seguro pois no keystore não é possivel armazenar
    fun saveServerPublicKey(data: String, publicKey: String): String {

        // logica de salvar no security sharedPreferences

        val bytes = Base64.decode(publicKey,Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(bytes)
            val keyFactory = KeyFactory.getInstance("RSA","BC")
        val public = keyFactory.generatePublic(keySpec)
        val result = encrypt(data,public)


        val valor = Base64.encodeToString(public.encoded,Base64.DEFAULT)
        Log.d(RsaKeystoreWrapper::class.simpleName, "CriptografiaResultado: $result")
        Log.d(RsaKeystoreWrapper::class.simpleName, "saveServerPublicKey: $public")
        Log.d(RsaKeystoreWrapper::class.simpleName, "Key Encoded: $valor")
        return result
    }

    fun saveKeyOldVersion(keyPair: KeyPair, alias: String) {
        val keystore = createKeyStore()
        var privateKeyEntry = keystore.getEntry(alias, null) as? KeyStore.PrivateKeyEntry
        if (privateKeyEntry == null) {
            privateKeyEntry = KeyStore.PrivateKeyEntry(keyPair.private, arrayOf())
            keystore.setEntry(alias, privateKeyEntry, null)
        }
    }

    @TargetApi(23)
    private fun getKeyGenParameterSpec(generator: KeyPairGenerator, alias: String) {
        val builder = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setKeySize(2048)
            .setUserAuthenticationRequired(false)
            .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
            .setRandomizedEncryptionRequired(false)
            .setDigests(KeyProperties.DIGEST_SHA256)

        generator.initialize(builder.build())
    }

    fun recoveryAsymmetricKeyPair(alias: String): KeyPair? {
        val keyStore: KeyStore = createKeyStore()

        val privateKey = keyStore.getKey(alias, null) as PrivateKey?
        val publicKey = keyStore.getCertificate(alias)?.publicKey

        return if (privateKey != null && publicKey != null) {
            KeyPair(publicKey, privateKey)
        } else {
            null
        }
    }

    fun recoveryPublic(alias: String): PublicKey {
        val keyStore = createKeyStore()
        val entry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
        return entry.certificate.publicKey as RSAPublicKey
    }

    private fun recoveryPrivateKey(alias: String): PrivateKey {
        val keyStore = createKeyStore()
        val entry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
        return entry.privateKey
    }

    fun removeKeyStoreKey(alias: String) = createKeyStore().deleteEntry(alias)

    fun encrypt(data: String, publicKey: PublicKey): String {
        val cipher: Cipher = Cipher.getInstance(RSA_TRANS)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val bytes = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(bytes, Base64.DEFAULT)

        Log.d(RsaKeystoreWrapper::class.simpleName, "encrypt: $bytes")
        Log.d(RsaKeystoreWrapper::class.simpleName, "Encodado: ${Base64.encodeToString(bytes, Base64.DEFAULT)}")
    }

    fun encrypt(data: String, alias: String): String {

        val publicKey = recoveryPublic(alias)
        val cipher: Cipher = Cipher.getInstance(RSA_TRANS)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val bytes = cipher.doFinal(data.toByteArray())

        Log.d(RsaKeystoreWrapper::class.simpleName, "encrypt: $bytes")
        Log.d(RsaKeystoreWrapper::class.simpleName, "Encodado: ${Base64.encodeToString(bytes, Base64.DEFAULT)}")
        Log.d(RsaKeystoreWrapper::class.simpleName, "Alias: $alias")
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    fun decrypt(data: String, alias: String): String {
        val privateKey = recoveryPrivateKey(alias)
        val cipher: Cipher = Cipher.getInstance(RSA_TRANS)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val encryptedData = Base64.decode(data, Base64.DEFAULT)
        val decodedData = cipher.doFinal(encryptedData)

        Log.d(RsaKeystoreWrapper::class.simpleName, "decoded Data: ${decodedData}")
        Log.d(RsaKeystoreWrapper::class.simpleName, "Resultado: ${String(decodedData,Charsets.UTF_8)}")
        Log.d(RsaKeystoreWrapper::class.simpleName, "Alias: $alias")
        return String(decodedData)
    }

    fun decrypt(data: String, privateKey: PrivateKey): String {
        val cipher: Cipher = Cipher.getInstance(RSA_TRANS)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val encryptedData = Base64.decode(data, Base64.DEFAULT)
        val decodedData = cipher.doFinal(encryptedData)

        Log.d(RsaKeystoreWrapper::class.simpleName, "decoded Data: ${decodedData}")
        Log.d(RsaKeystoreWrapper::class.simpleName, "Resultado: ${String(decodedData,Charsets.UTF_8)}")
        return String(decodedData)
    }

}

fun hasMarshmallow() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
