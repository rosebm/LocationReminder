package com.rosalynbm.locationreminder.authentication

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import timber.log.Timber

class AuthenticationViewModel(private val context: Context,
                              private val sharedPref: SharedPreferences
): ViewModel() {

    init {
        FirebaseAuth.getInstance()
    }

    private val currentUser = MutableLiveData<FirebaseUser>()
    private val authenticationState = MutableLiveData<AuthenticationState>()

    fun getCurrentUser(): LiveData<FirebaseUser> = currentUser
    fun authenticationStateLiveData(): LiveData<AuthenticationState> = authenticationState

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    fun authenticationState(): AuthenticationState {
        var authenticated: AuthenticationState = AuthenticationState.UNAUTHENTICATED

        FirebaseAuth.AuthStateListener { firebaseAuth ->
            Timber.d("CurrentUser: ${firebaseAuth.currentUser}")

            authenticated = if (firebaseAuth.currentUser != null)
                AuthenticationState.AUTHENTICATED
            else
                AuthenticationState.UNAUTHENTICATED
        }

        authenticationState.value = authenticated
        return authenticated
    }

    fun setUserAuthenticated(value: Boolean) {
        sharedPref.edit().putBoolean("user_authenticate_state", value).apply()
    }

    fun isUserAuthenticated() = sharedPref.getBoolean("user_authenticate_state", false)

}