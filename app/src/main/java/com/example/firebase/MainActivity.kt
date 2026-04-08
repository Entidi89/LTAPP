package com.example.firebase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.firebase.ui.theme.FirebaseTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val movieViewModel: MovieViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Khởi tạo Firebase thủ công nếu nó chưa được khởi tạo tự động
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            // Log lỗi hoặc xử lý nếu cần
        }

        enableEdgeToEdge()
        setContent {
            FirebaseTheme {
                MovieApp(movieViewModel)
            }
        }
    }
}
