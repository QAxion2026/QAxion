package com.qaxion.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.qaxion.app.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate logo
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade)

        binding.ivLogo.startAnimation(fadeIn)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.tvTagline.startAnimation(slideUp)
            binding.tvTagline.visibility = android.view.View.VISIBLE
        }, 400)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.progressBar.visibility = android.view.View.VISIBLE
        }, 800)

        // Navigate to selector after 2.5s
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, SelectorActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }, 2800)
    }
}
