package com.hamric.gituser

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private val greetingService: GreetingService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvMessage = findViewById<TextView>(R.id.tvMessage)
        val btnGreet = findViewById<Button>(R.id.btnGreet)

        tvMessage.text = greetingService.greet("Developer")

        btnGreet.setOnClickListener {
            tvMessage.text = greetingService.greet("Koin User")
        }
    }
}