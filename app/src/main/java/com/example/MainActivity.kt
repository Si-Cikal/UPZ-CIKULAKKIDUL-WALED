package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.attemptAutoSignIn
import com.example.auth.authStateFlow
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.UpzMainShell
import com.example.ui.theme.UpzCikulakTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.auto(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT,
            ),
        )
        setContent {
            UpzCikulakTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    var checkingAutoSignIn by remember { mutableStateOf(auth.currentUser == null) }

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { checkingAutoSignIn = false },
                onUnauthenticated = { checkingAutoSignIn = false },
                scope = scope,
            )
        } else {
            checkingAutoSignIn = false
        }
    }

    val user = currentUser
    when {
        checkingAutoSignIn && user == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        user == null -> {
            LoginScreen(
                onAuthSuccess = { checkingAutoSignIn = false },
            )
        }
        else -> {
            key(user.uid) {
                UpzMainShell(
                    currentUserId = user.uid,
                    officerDisplayName = user.displayName,
                    officerEmail = user.email,
                    officerPhotoUrl = user.photoUrl?.toString(),
                )
            }
        }
    }
}
