package com.rosalynbm.locationreminder.authentication

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.rosalynbm.locationreminder.BuildConfig
import com.rosalynbm.locationreminder.R
import com.rosalynbm.locationreminder.locationreminders.RemindersActivity
import com.rosalynbm.locationreminder.utils.Variables
import kotlinx.android.synthetic.main.activity_authentication.*
import org.koin.android.ext.android.inject
import timber.log.Timber


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity(), View.OnClickListener {

    val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build(),
    )
    private val authenticationViewModel: AuthenticationViewModel by inject()
    private lateinit var preferences: SharedPreferences

    // Arbitrary request code to identify the request when the result is returned in onActivityResult
    private val SIGN_IN_REQUEST_CODE = 1101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        authLoginButton.setOnClickListener(this)
        // init timber
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

        observeAuthenticationState()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        // SIGN_IN_REQUEST_CODE is the request code passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)

            // Successfully signed in
            when(resultCode) {
                Activity.RESULT_OK -> {
                    authenticationViewModel.setUserAuthenticated(true)

                    val idpResponse = IdpResponse.fromResultIntent(data)
                    startActivity(
                        Intent(this, RemindersActivity::class.java)
                            .putExtra("my_token", idpResponse?.idpToken)
                    )
                    finish()
                }

                else -> {

                    // Sign in failed
                    if (response == null) {
                        // User pressed back button
                        authLoginButton.snack(R.string.auth_sign_in_cancelled, Snackbar.LENGTH_LONG,{})
                        return
                    }
                    if (response.error?.errorCode == ErrorCodes.NO_NETWORK) {
                        authLoginButton.snack(R.string.no_internet_connection, Snackbar.LENGTH_LONG,{})
                        return
                    } else {
                        authLoginButton.snack(R.string.unknown_error, Snackbar.LENGTH_LONG,{})
                    }
                    Timber.e("Sign-in error: ${response.error}")
                }
            }

        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.authLoginButton -> {
                if (Variables.isNetworkConnected)
                    authenticateAndLogin()
                else
                    Toast.makeText(this, getString(R.string.no_internet_connection),
                        Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun authenticateAndLogin() {
        when (authenticationViewModel.authenticationState()) {
            AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                Timber.d("User signed in")

                startReminderActivity()
                finish()
            }

            AuthenticationViewModel.AuthenticationState.UNAUTHENTICATED -> {
                // not signed in
                Timber.d("User not signed in")

                startActivityForResult(
                    // Get an instance of AuthUI based on the default app
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                    SIGN_IN_REQUEST_CODE)
            }

            else ->
                Timber.d(getString(R.string.invalid_authentication))
        }
    }

    private fun observeAuthenticationState() {
        val userAuthenticated = authenticationViewModel.isUserAuthenticated()

        if (userAuthenticated) {
            startReminderActivity()
        }
    }

    private fun startReminderActivity() {
        startActivity(
            Intent(this, RemindersActivity::class.java)
            //.putExtra("my_token", auth.currentUser.)
        )
    }

    private inline fun View.snack(@StringRes messageRes: Int, length: Int = Snackbar.LENGTH_LONG, f: Snackbar.() -> Unit) {
        val snack = Snackbar.make(this, messageRes, length)
        snack.f()
        snack.show()
    }

}


