package com.example.simonsaysapp

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.simonsaysapp.MainActivity
import com.example.simonsaysapp.databinding.ActivityMainLoggedBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

enum class ProviderType{
    BASIC
   // GOOGLE
}

//private val db = FirebaseFirestore.getInstance()

class MainLogged : AppCompatActivity() {
    lateinit var binding : ActivityMainLoggedBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainLoggedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setup()
    }

    private fun setup() {

        //canviem el títol de l'aplicació
        title = "Flower Says"
        // que comenci la musica al iniciar la app
        val mp = MediaPlayer.create(this, R.raw.sound_background)
        mp.start()
        //click al botó inscriu-te
        binding.signUpButton.setOnClickListener {
            if (binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    binding.emailEditText.text.toString(),
                    binding.passwordEditText.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        mp.pause()
                        showHome(it.result?.user?.email ?: "")
                    } else {
                        showAlert()
                    }
                }
            }
        }
        //clic al botó Accedir
        binding.logInButton.setOnClickListener {
            if (binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    binding.emailEditText.text.toString(),
                    binding.passwordEditText.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        mp.stop() // parar la música quan conectem
                        showHome(it.result?.user?.email ?: "")
                    } else {
                        showAlert()
                    }
                }
            }
        }
    }

    //falla la autenticació d'usuaris
    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("ERROR!")
        builder.setMessage("Error with the user!")
        builder.setPositiveButton("Acept", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //l'autenticació funciona correctament
    private fun showHome(email: String) {
        val homeIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("email", email)
           // putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }
}



