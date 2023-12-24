package com.example.wclchat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.wclchat.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SignInAct : ComponentActivity() {
    lateinit var launcher: ActivityResultLauncher<Intent>
    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {

                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account.idToken!!)
                }

            } catch (e: ApiException) {
                Log.d("MyLog", "Api exception")
            }
        }

        binding.bSignIn.setOnClickListener {
            signInWithGoogle()
        }
        checkAuthState()
    }

    private fun getClient(): GoogleSignInClient {
       val gso = GoogleSignInOptions
           .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
           .requestIdToken(getString(R.string.default_web_client_id))
           .requestEmail()
           .build()
        return GoogleSignIn.getClient(this, gso)
    }

    private fun signInWithGoogle() {
        val signInClient = getClient()
        launcher.launch(signInClient.signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful) {
                Log.d("MyLog", "Google signIn done")
                checkAuthState()
                checkUserPreferences() // Проверяем предпочтения пользователя
            } else {
                Log.d("MyLog", "Google signIn error")
            }
        }
    }

    private fun checkAuthState() {
        if (auth.currentUser != null) {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }
    }

    private fun checkUserPreferences() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("SignInAct", "Checking user preferences for user ID: $userId")
        if (userId != null) {
            val databaseReference = Firebase.database.getReference("usersPreferences")
            databaseReference.child(userId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Log.d("SignInAct", "User preferences not found, opening PreferencesActivity")
                        openPreferencesScreen()
                    } else {
                        Log.d("SignInAct", "User preferences found, opening MainActivity")
                        openMainScreen()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("SignInAct", "Database error: ${error.message}")
                }
            })
        } else {
            Log.d("SignInAct", "User ID is null, cannot check preferences")
        }
    }
    private fun openPreferencesScreen() {
        val intent = Intent(this, PreferencesActivity::class.java)
        startActivity(intent)
        finish() // закрыть текущую активность после перехода
    }


    private fun openMainScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // закрыть текущую активность после перехода
    }


}
