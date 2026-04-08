package com.example.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MovieApp(viewModel: MovieViewModel) {
    val navController = rememberNavController()
    val user by viewModel.user

    NavHost(
        navController = navController,
        startDestination = if (user == null) "login" else "movieList"
    ) {
        composable("login") {
            LoginScreen(viewModel) {
                navController.navigate("movieList") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
        composable("movieList") {
            MovieListScreen(viewModel, navController)
        }
        composable("movieDetail/{movieId}") { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            MovieDetailScreen(movieId, viewModel, navController)
        }
        composable("myTickets") {
            MyTicketsScreen(viewModel)
        }
    }
}

@Composable
fun LoginScreen(viewModel: MovieViewModel, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Movie Ticket App", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Vui lòng nhập đầy đủ Email và Password", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.login(email, password) { success, message ->
                    if (success) onLoginSuccess()
                    else Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Login")
        }
        TextButton(onClick = {
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Vui lòng nhập Email và Password để đăng ký", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.register(email, password) { success, message ->
                    if (success) {
                        Toast.makeText(context, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }) {
            Text("Register")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(viewModel: MovieViewModel, navController: NavController) {
    val movies by viewModel.movies.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movies") },
                actions = {
                    IconButton(onClick = { navController.navigate("myTickets") }) {
                        Icon(Icons.Default.AccountBox, "My Tickets")
                    }
                    IconButton(onClick = { viewModel.logout(); navController.navigate("login") }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout")
                    }
                }
            )
        }
    ) { padding ->
        if (movies.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Đang tải phim hoặc dữ liệu trống...")
                    Button(onClick = { viewModel.addSampleMovies() }) {
                        Text("Thêm phim mẫu")
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(movies) { movie ->
                    MovieItem(movie) {
                        navController.navigate("movieDetail/${movie.id}")
                    }
                }
            }
        }
    }
}

@Composable
fun MovieItem(movie: Movie, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onClick() }) {
        Row(modifier = Modifier.padding(16.dp)) {
            AsyncImage(model = movie.imageUrl, contentDescription = null, modifier = Modifier.size(100.dp), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(text = movie.title, style = MaterialTheme.typography.titleLarge)
                Text(text = movie.genre, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(movieId: String, viewModel: MovieViewModel, navController: NavController) {
    val movies by viewModel.movies.collectAsState()
    val movie = movies.find { it.id == movieId }
    var seatValue by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(topBar = { TopAppBar(title = { Text(movie?.title ?: "Movie Detail") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            movie?.let { movieItem ->
                AsyncImage(model = movieItem.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.Crop)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = movieItem.description, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = seatValue,
                    onValueChange = { seatValue = it },
                    label = { Text("Seat (e.g. A1)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    if (seatValue.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập số ghế", Toast.LENGTH_SHORT).show()
                    } else {
                        val ticket = Ticket(movieTitle = movieItem.title, showtime = Timestamp.now(), seat = seatValue)
                        viewModel.bookTicket(ticket) { success ->
                            if (success) {
                                Toast.makeText(context, "Đặt vé thành công!", Toast.LENGTH_SHORT).show()
                                showNotification(context, movieItem.title)
                                navController.navigate("myTickets")
                            }
                        }
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Book Now")
                }
            }
        }
    }
}

fun showNotification(context: Context, movieTitle: String) {
    val channelId = "movie_reminder"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Nhắc nhở giờ chiếu", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Đặt vé thành công!")
        .setContentText("Bạn đã đặt vé phim $movieTitle. Chúc bạn xem phim vui vẻ!")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    notificationManager.notify(1, builder.build())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(viewModel: MovieViewModel) {
    val tickets by viewModel.myTickets.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Scaffold(topBar = { TopAppBar(title = { Text("My Tickets") }) }) { padding ->
        if (tickets.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tickets found")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(tickets) { ticket ->
                    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(ticket.movieTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Showtime: ${dateFormat.format(ticket.showtime.toDate())}")
                            Text("Seat: ${ticket.seat}")
                        }
                    }
                }
            }
        }
    }
}
