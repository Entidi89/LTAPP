package com.example.firebase

import com.google.firebase.Timestamp

data class Movie(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val genre: String = ""
)

data class Showtime(
    val id: String = "",
    val movieId: String = "",
    val dateTime: Timestamp = Timestamp.now(),
    val price: Double = 0.0
)

data class Ticket(
    val id: String = "",
    val userId: String = "",
    val movieTitle: String = "",
    val showtime: Timestamp = Timestamp.now(),
    val seat: String = ""
)
