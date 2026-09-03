package com.jovanmosurovic.personalfinancemanager.ui

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.activity.compose.LocalActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jovanmosurovic.personalfinancemanager.R

private const val BIOMETRIC_AUTHENTICATORS =
    BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL

internal class BiometricAuthenticator(private val context: Context) {
    fun canAuthenticate(): Boolean {
        val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BIOMETRIC_AUTHENTICATORS
        } else {
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        }
        return BiometricManager.from(context).canAuthenticate(authenticators) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onFailure()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
            }
        )
        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.biometric_prompt_title))
            .setSubtitle(context.getString(R.string.biometric_prompt_description))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            promptInfoBuilder.setAllowedAuthenticators(BIOMETRIC_AUTHENTICATORS)
        } else {
            promptInfoBuilder
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText(context.getString(R.string.cancel))
        }

        try {
            prompt.authenticate(promptInfoBuilder.build())
        } catch (_: IllegalStateException) {
            onFailure()
        }
    }
}

@Composable
internal fun BiometricLock(
    enabled: Boolean,
    authenticator: BiometricAuthenticator,
    content: @Composable () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = LocalActivity.current
    var isUnlocked by remember { mutableStateOf(!enabled) }
    var shouldAuthenticate by remember { mutableStateOf(enabled) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var authenticationFailed by remember { mutableStateOf(false) }

    LaunchedEffect(enabled) {
        if (enabled) {
            isUnlocked = false
            shouldAuthenticate = true
            authenticationFailed = false
        } else {
            isUnlocked = true
            shouldAuthenticate = false
            authenticationFailed = false
        }
    }

    DisposableEffect(lifecycleOwner, enabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (enabled) {
                when (event) {
                    Lifecycle.Event.ON_STOP -> {
                        isUnlocked = false
                        authenticationFailed = false
                    }

                    Lifecycle.Event.ON_START -> shouldAuthenticate = true
                    else -> Unit
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(enabled, shouldAuthenticate, activity) {
        if (enabled && shouldAuthenticate && !isUnlocked && !isAuthenticating) {
            shouldAuthenticate = false
            if (activity !is FragmentActivity) {
                authenticationFailed = true
            } else {
                isAuthenticating = true
                authenticator.authenticate(
                    activity = activity,
                    onSuccess = {
                        isAuthenticating = false
                        isUnlocked = true
                        authenticationFailed = false
                    },
                    onFailure = {
                        isAuthenticating = false
                        authenticationFailed = true
                    }
                )
            }
        }
    }

    if (!enabled || isUnlocked) {
        content()
    } else {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.biometric_lock_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = stringResource(
                        if (authenticationFailed) {
                            R.string.biometric_authentication_failed
                        } else {
                            R.string.biometric_lock_description
                        }
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Button(
                    enabled = !isAuthenticating,
                    onClick = {
                        authenticationFailed = false
                        shouldAuthenticate = true
                    },
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Text(stringResource(R.string.biometric_unlock))
                }
            }
        }
    }
}
