package com.preonboarding.videorecorder.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.preonboarding.videorecorder.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}