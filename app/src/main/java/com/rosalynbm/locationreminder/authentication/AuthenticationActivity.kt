package com.rosalynbm.locationreminder.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.rosalynbm.locationreminder.BuildConfig
import com.rosalynbm.locationreminder.R
import com.rosalynbm.locationreminder.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_authentication.*
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

    // Arbitrary request code to identify the request when the result is returned in onActivityResult
    private val RC_SIGN_IN = 1101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        authLoginButton.setOnClickListener(this)
        // init timber
        //if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google



//          TODO: If the user was authenticated, send him to RemindersActivity

//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            Timber.d("ROS RC_SIGN_IN, result code $resultCode")
            Timber.d("ROS RC_SIGN_IN, response ${response?.email}")

            // Successfully signed in
            when(resultCode) {
                Activity.RESULT_OK -> {
                    val idpResponse = IdpResponse.fromResultIntent(data)
                    startActivity(
                        Intent(this, RemindersActivity::class.java)
                            .putExtra("my_token", idpResponse!!.idpToken)
                    )
                    finish()
                }

                else -> {

                    // Sign in failed
                    if (response == null) {
                        // User pressed back button
                        //showSnackbar(R.string.sign_in_cancelled)
                        return
                    }
                    if (response.error?.errorCode == ErrorCodes.NO_NETWORK) {
                        //showSnackbar(R.string.no_internet_connection)
                        return
                    }
                    //showSnackbar(R.string.unknown_error)
                    Timber.e("ROS Sign-in error: ${response.error}")
                }
            }

        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.authLoginButton -> {
                Timber.d("ROS authLoginButton click")
                val auth = FirebaseAuth.getInstance()
                if (auth.currentUser != null) {
                    Timber.d("ROS signed in")
                    Timber.d("ROS signed as ${auth.currentUser?.email}")

                    startActivity(
                            Intent(this, RemindersActivity::class.java)
                                    //.putExtra("my_token", auth.currentUser.)
                    )
                    finish()
                } else {
                    // not signed in
                    Timber.d("ROS not signed in")

                    // already signed in
                    startActivityForResult(
                            // Get an instance of AuthUI based on the default app
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN)
                }
            }
        }
    }
}
