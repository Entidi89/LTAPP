package com.example.firebase

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MovieViewModel : ViewModel() {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies

    private val _myTickets = MutableStateFlow<List<Ticket>>(emptyList())
    val myTickets: StateFlow<List<Ticket>> = _myTickets

    private val _user = mutableStateOf<FirebaseUser?>(null)
    val user: State<FirebaseUser?> = _user

    init {
        try {
            _user.value = auth.currentUser
            fetchMovies()
            fetchMyTickets()
        } catch (e: Exception) {}
    }

    fun addSampleMovies() {
        viewModelScope.launch {
            val sampleMovies = listOf(
                Movie(id = "1", title = "Avengers: Endgame", genre = "Action, Sci-Fi", description = "After the devastating events of Infinity War, the universe is in ruins.", imageUrl = "https://image.tmdb.org/t/p/w500/or06vSqzIBYr3GvDbq0fsIVTq7K.jpg"),
                Movie(id = "2", title = "The Lion King", genre = "Animation, Adventure", description = "Simba adores his father, King Mufasa, and takes to heart his own royal destiny.", imageUrl = "https://image.tmdb.org/t/p/w500/dzBT9mAg2hBgT0p9GvI699tmO1E.jpg"),
                Movie(id = "3", title = "Interstellar", genre = "Drama, Sci-Fi", description = "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.", imageUrl = "https://image.tmdb.org/t/p/w500/gEU2QniE6EwfVDxjSrkp9pYUni6.jpg"),
                Movie(id = "4", title = "Joker", genre = "Crime, Drama", description = "In Gotham City, mentally troubled comedian Arthur Fleck is disregarded and mistreated by society.", imageUrl = "https://image.tmdb.org/t/p/w500/udDclKVX2qQZ6JAFnOQvcRUmI1o.jpg"),
                Movie(id = "5", title = "Spider-Man: No Way Home", genre = "Action, Adventure", description = "With Spider-Man's identity now revealed, Peter asks Doctor Strange for help.", imageUrl = "https://image.tmdb.org/t/p/w500/1g0zzvWwsF7139SjcsInpStatus.jpg"),
                Movie(id = "6", title = "Inception", genre = "Action, Sci-Fi", description = "A thief who steals corporate secrets through the use of dream-sharing technology.", imageUrl = "https://image.tmdb.org/t/p/w500/edv5CZvR0Yv9uO6Yv9vO6Yv9vO6.jpg"),
                Movie(id = "7", title = "Parasite", genre = "Drama, Thriller", description = "Greed and class discrimination threaten the newly formed symbiotic relationship between the wealthy Park family and the destitute Kim clan.", imageUrl = "https://image.tmdb.org/t/p/w500/7IiTT00ghvB3YpIqSabaS9zRnuw.jpg"),
                Movie(id = "8", title = "The Dark Knight", genre = "Action, Crime", description = "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham.", imageUrl = "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDp9asPo9vC9o9Y3YvB.jpg")
            )
            try {
                for (movie in sampleMovies) {
                    db.collection("movies").document(movie.id).set(movie).await()
                }
                fetchMovies() // Tải lại sau khi thêm
            } catch (e: Exception) {}
        }
    }

    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    _user.value = auth.currentUser
                    if (task.isSuccessful) {
                        fetchMyTickets()
                        onResult(true, "Success")
                    } else {
                        onResult(false, task.exception?.message ?: "Login Failed")
                    }
                }
        } catch (e: Exception) {
            onResult(false, e.message ?: "Error")
        }
    }

    fun register(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        try {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    _user.value = auth.currentUser
                    if (task.isSuccessful) {
                        onResult(true, "Success")
                    } else {
                        onResult(false, task.exception?.message ?: "Registration Failed")
                    }
                }
        } catch (e: Exception) {
            onResult(false, e.message ?: "Error")
        }
    }

    private fun fetchMovies() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("movies").get().await()
                _movies.value = snapshot.toObjects(Movie::class.java)
            } catch (e: Exception) {}
        }
    }

    fun fetchMyTickets() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val snapshot = db.collection("tickets")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                _myTickets.value = snapshot.toObjects(Ticket::class.java)
            } catch (e: Exception) {}
        }
    }

    fun bookTicket(ticket: Ticket, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                db.collection("tickets").add(ticket.copy(userId = userId)).await()
                fetchMyTickets()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun logout() {
        try {
            auth.signOut()
            _user.value = null
            _myTickets.value = emptyList()
        } catch (e: Exception) {}
    }
}
