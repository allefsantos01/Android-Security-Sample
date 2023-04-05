package com.developer.allefsousa.totp.cryptography

import android.annotation.TargetApi
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.security.keystore.UserNotAuthenticatedException
import android.util.Base64
import android.util.Log
import java.io.ByteArrayInputStream
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import java.util.*
import javax.crypto.Cipher


class RsaKeystoreWrapper {
    companion object {
        const val RSA_TRANS = "RSA/ECB/PKCS1Padding"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "keyPP"
    }

    private var signatureResult: String = ""


    private fun createKeyStore(): KeyStore {
        val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore
    }

    fun createAsymmetricKeyPair(alias: String): KeyPair {
        val generator: KeyPairGenerator

        if (hasMarshmallow()) {
            generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)
           getKeyGenParameterSpec(generator, alias)
        } else {
            generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
            generator.initialize(2048)
        }

        return generator.generateKeyPair()
    }

    @TargetApi(23)
    fun signData(data: String, alias: String): String {
        try {
            //We get the Keystore instance
            val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }

            //Retrieves the private key from the keystore
            val privateKey: PrivateKey = keyStore.getKey(alias, null) as PrivateKey

            //We sign the data with the private key. We use RSA algorithm along SHA-256 digest algorithm
            val signature: ByteArray? = Signature.getInstance("SHA256withRSA").run {
                initSign(privateKey)
                update(data.toByteArray())
                sign()
            }

            if (signature != null) {
                //We encode and store in a variable the value of the signature
                signatureResult = Base64.encodeToString(signature, Base64.DEFAULT)
                return signatureResult
            }

        } catch (e: UserNotAuthenticatedException) {
            throw RuntimeException(e)
            //Exception thrown when the user has not been authenticated.
            //showAuthenticationScreen()
        } catch (e: KeyPermanentlyInvalidatedException) {
            throw RuntimeException(e)
            //Exception thrown when the key has been invalidated for example when lock screen has been disabled.
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        return ""
    }

    fun verifyData(data: String, alias: String): Boolean {
        //We get the Keystore instance
        val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

        //We get the certificate from the keystore
        val certificate: Certificate? = keyStore.getCertificate(alias)

        if (certificate != null) {
            //We decode the signature value
            val signature: ByteArray = Base64.decode(signatureResult, Base64.DEFAULT)

            //We check if the signature is valid. We use RSA algorithm along SHA-256 digest algorithm
            val isValid: Boolean = Signature.getInstance("SHA256withRSA").run {
                initVerify(certificate)
                update(data.toByteArray())
                verify(signature)
            }

            return isValid
        }
        return false
    }

    fun saveServerPublicKey(alias: String, publicKey: String) {
        val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val cert: X509Certificate =
            cf.generateCertificate(ByteArrayInputStream(publicKey.toByteArray())) as X509Certificate
        keyStore.setCertificateEntry(alias, cert)
    }

    fun savePublicKey(keyPair: KeyPair, alias: String) {
        val keystore = createKeyStore()
        var privateKeyEntry = keystore.getEntry(alias, null) as? KeyStore.PrivateKeyEntry
        if (privateKeyEntry == null) {
            privateKeyEntry = KeyStore.PrivateKeyEntry(keyPair.private, arrayOf())
            keystore.setEntry(alias, privateKeyEntry, null)
        }
    }

    @TargetApi(23)
    private fun getKeyGenParameterSpec(generator: KeyPairGenerator, alias: String) {
        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(2048)
            .setUserAuthenticationRequired(false)
            .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
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

    private fun recoveryPublic(alias: String): PublicKey {
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

}

fun hasMarshmallow() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
