package com.example.simonsaysapp

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.example.simonsaysapp.databinding.ActivityMainBinding
import com.google.common.primitives.Booleans
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    //  crear una connexió amb la base de dades:
    private val db = FirebaseFirestore.getInstance()
    private var clicks = 0
    private var btns = arrayOfNulls<View>(5)
    private var sounds = arrayOfNulls<MediaPlayer>(5)
    private val sons = mutableListOf<Int>()
    private lateinit var error: MediaPlayer
//    private var firstTime = true
    private var points = 0
    private var record = 0
    private var mail = "null"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        val email = bundle?.getString("email")



        mail = email.toString()

        db.collection("users").document(mail).get().addOnSuccessListener {
            binding.numRecordTextView.text = it.get("score") as String?
        }

        sounds[0] = MediaPlayer.create(this, R.raw.blue_sound)
        sounds[1] = MediaPlayer.create(this, R.raw.red_sound)
        sounds[2] = MediaPlayer.create(this, R.raw.yellow_sound)
        sounds[3] = MediaPlayer.create(this, R.raw.pink_sound)
        sounds[4] = MediaPlayer.create(this, R.raw.violet_sound)
        error = MediaPlayer.create(this, R.raw.error)

        btns[0] = binding.bluecircle
        btns[1] = binding.redcircle
        btns[2] = binding.yellowcircle
        btns[3] = binding.pinkcircle
        btns[4] = binding.violetcircle

        setup(email ?: "")

        // afegim els sons a cada imatge:
        addSoundImage(R.id.bluecircle, R.raw.blue_sound, 0)
        addSoundImage(R.id.redcircle, R.raw.red_sound, 1)
        addSoundImage(R.id.yellowcircle, R.raw.yellow_sound, 2)
        addSoundImage(R.id.pinkcircle, R.raw.pink_sound, 3)
        addSoundImage(R.id.violetcircle, R.raw.violet_sound, 4)

        // Bloquejem les imatges dels botons del joc fins a que comenci el joc
        activateImages(false)

        // afegim el botó START
        val button: Button = findViewById(R.id.buttonStart)
        button.setOnClickListener {

            val mp = MediaPlayer.create(this, R.raw.center_pressed)
//          mp.setVolume(1.0f,1.0f)
            mp.start()

            activateImages(true)
            delay(950) {
                playFlower()
                button.isEnabled = false
            }
        }
    }

    enum class ProviderType{
        BASIC
    }

    inline fun delay(delay: Long, crossinline completion: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            completion()
        }, delay)
    }

    private fun playHuman(valor: Int) {
        if(sons[clicks]==valor) {
            clicks ++
            if(clicks == sons.size) {
              //  updatePoints()
                points += 2
                delay(650L * valor) {
                    binding.textViewNumScore.text = points.toString()
                    playFlower()
                }
            }
        } else {
            delay(350L * valor) {
                error?.start()
                sons.clear()
                binding.textViewNumScore.text = "0"
                activateImages(false)
                val button: Button = findViewById(R.id.buttonStart)
                button.isEnabled = true

                if (points > record) {
                    // actualitzem la puntuació del record:
                    binding.numRecordTextView.text = points.toString()
                    record = points

                    // guardem en la bbdd el record:
                    db.collection("users").document(mail).set(hashMapOf("score"
                            to binding.numRecordTextView.text.toString()))
                }
            }

           // alerta
        }
    }

    private fun playFlower() {
        binding.textViewFlowerTitle.text = "Flower Says"
        activateImages(false)
        sons?.add(numRandom())

        for(i in 0 until(sons.size)) {
            delay(650L*i) {
                game(sons[i])
            }
        }
        clicks = 0
        delay(650L * sons.size) {
            binding.textViewFlowerTitle.text = "Your turn"
            activateImages(true)
        }
    }

    // aconseguir un numero random entre 0 i 4 (1-5)
    private fun numRandom(): Int{
        return (0..4).random()
    }

    fun game(index: Int) {
        val firstNumber: Int
        val secondNumber: Int

        when (index) {
            0 -> {
                firstNumber = R.drawable.azulluz
                secondNumber = R.drawable.btn_image_blue
            }
            1 -> {
                firstNumber = R.drawable.rojoluz
                secondNumber = R.drawable.btn_image_red
            }
            2 -> {
                firstNumber = R.drawable.amarilloluz
                secondNumber = R.drawable.btn_image_yellow
            }
            3 -> {
                firstNumber = R.drawable.rosaluz
                secondNumber = R.drawable.btn_image_pink
            }
            else -> {
                firstNumber = R.drawable.lilaluz
                secondNumber = R.drawable.btn_image_violet
            }
        }

        btns[index]?.setBackgroundResource(firstNumber)
        sounds[index]?.setVolume(1.0f, 1.0f)
        sounds[index]?.start()

        delay(350) {
            btns[index]?.setBackgroundResource(secondNumber)
        }
    }


//    private fun addSoundButton(button: Button, sound: Int) {
//        button.setOnClickListener {
//            val mp = MediaPlayer.create(this, sound)
//            mp.setVolume(1.0f,1.0f)
//            mp.start()
//        }
//    }

    private fun addSoundImage(image: Int, sound: Int, num: Int) {
        val imageClicked = findViewById<ImageView>(image)
        imageClicked.setOnClickListener {
            val mp = MediaPlayer.create(this, sound)
            mp.setVolume(1.0f,1.0f)
            mp.start()
            playHuman(num)
        }
    }

    private fun activateImages(value: Boolean) {
        activateImage(R.id.bluecircle, value)
        activateImage(R.id.redcircle, value)
        activateImage(R.id.yellowcircle, value)
        activateImage(R.id.pinkcircle, value)
        activateImage(R.id.violetcircle,value)
    }

    private fun activateImage(image: Int, value: Boolean) {
        val imageClicked = findViewById<ImageView>(image)
        imageClicked.isEnabled = value
    }

    private fun setup(email: String) {
        title = "Let's play!"
        binding.emailTextView.text = email
        //click al botó Tancar sessió
        binding.logOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            // i tornem a la pantalla d'inici
            onBackPressed()
            val mp = MediaPlayer.create(this, R.raw.sound_background)
            mp.start()
        }
    }
}