package com.phacufpe.studying

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.material.snackbar.Snackbar
import com.phacufpe.studying.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var oneTapResult: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        oneTapResult = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            val snackbar = Snackbar.make(binding.root, "", Snackbar.LENGTH_INDEFINITE)
            var message = ""
            val tag = "OneTap"
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                message = "idToken: $idToken"
                snackbar.setText(message)
                Log.d(tag, message)

                navigateToMain()
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    CommonStatusCodes.CANCELED -> {
                        message = "One-tap dialog was closed."
                        Log.d(tag, message)
                        snackbar.setText(message)
                        snackbar.show()
                    }
                    CommonStatusCodes.NETWORK_ERROR -> {
                        message = "One-tap encountered a network error."
                        Log.d(tag, message)
                        snackbar.setText(message)
                        snackbar.show()
                    }
                    else -> {
                        message = "Coulnd't get credential from result."
                        Log.d(tag, message)
                        snackbar.setText(message)
                        snackbar.show()
                    }
                }
            }
        }

        val loginButton = findViewById<SignInButton>(R.id.loginButton)
        loginButton.setSize(SignInButton.SIZE_WIDE)

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            ).build()

        loginButton.setOnClickListener { displaySignIn() }
    }

    private fun displaySignIn() {
        val tag = "ButtonClick"
        oneTapClient.beginSignIn(signInRequest).addOnSuccessListener(this) { result ->
            try {
                val intentBuild = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                oneTapResult.launch(intentBuild)
            } catch (exception: IntentSender.SendIntentException) {
                Log.e(tag, "Couldn't start One Tap UI: ${exception.localizedMessage}")
            }
        }
            .addOnFailureListener(this) { exception ->
                exception.localizedMessage?.let { message -> Log.d(tag, message) }
            }
    }

    private fun navigateToMain() {
        finish()
        val myIntent = Intent(this, MainActivity::class.java)
        startActivity(myIntent)
    }
}