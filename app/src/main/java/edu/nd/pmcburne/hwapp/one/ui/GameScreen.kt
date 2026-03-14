package edu.nd.pmcburne.hwapp.one.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.nd.pmcburne.hwapp.one.GameViewModel
import edu.nd.pmcburne.hwapp.one.database.GameEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val games = viewModel.games
    val isLoading = viewModel.isLoading
    val isOffline = viewModel.isOffline
    val selectedDate = viewModel.selectedDate
    val selectedGender = viewModel.selectedGender

    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NCAA Basketball", fontWeight = FontWeight.Black) },
                actions = {
                    IconButton(onClick = { viewModel.onRefresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Bar
            Surface(tonalElevation = 3.dp, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(formatDateForDisplay(selectedDate))
                    }

                    SingleChoiceSegmentedButtonRow {
                        SegmentedButton(
                            selected = selectedGender == "men",
                            onClick = { viewModel.onGenderSelected("men") },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) { Text("Men") }
                        SegmentedButton(
                            selected = selectedGender == "women",
                            onClick = { viewModel.onGenderSelected("women") },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) { Text("Women") }
                    }
                }
            }

            if (isOffline) {
                Surface(color = MaterialTheme.colorScheme.errorContainer) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.WifiOff, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(Modifier.width(8.dp))
                        Text("OFFLINE MODE - CACHED DATA", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.onRefresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                if (!isLoading && games.isEmpty()) {
                    EmptyState(selectedDate)
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(games, key = { it.uid }) { game ->
                            GameCard(game = game, isMen = selectedGender == "men")
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        SimpleDatePicker(selectedDate) { newDate ->
            viewModel.onDateSelected(newDate)
            showDatePicker = false
        }
    }
}

@Composable
fun GameCard(game: GameEntity, isMen: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                StatusLabel(game, isMen)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamDisplay(
                    shortName = game.awayTeamShort,
                    fullName = game.awayTeamName,
                    score = game.awayScore,
                    isWinner = game.awayWinner && game.state == "post",
                    label = "AWAY",
                    labelColor = MaterialTheme.colorScheme.secondary,
                    alignment = Alignment.Start,
                    modifier = Modifier.weight(1f),
                    showScore = game.state != "pre"
                )

                Text(
                    text = if (game.state == "pre") "VS" else "—",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                TeamDisplay(
                    shortName = game.homeTeamShort,
                    fullName = game.homeTeamName,
                    score = game.homeScore,
                    isWinner = game.homeWinner && game.state == "post",
                    label = "HOME",
                    labelColor = MaterialTheme.colorScheme.primary,
                    alignment = Alignment.End,
                    modifier = Modifier.weight(1f),
                    showScore = game.state != "pre"
                )
            }
        }
    }
}

@Composable
fun TeamDisplay(
    shortName: String,
    fullName: String,
    score: Int,
    isWinner: Boolean,
    label: String,
    labelColor: Color,
    alignment: Alignment.Horizontal,
    modifier: Modifier,
    showScore: Boolean
) {
    val winnerGreen = Color(0xFF2E7D32)
    val contentColor = if (isWinner) winnerGreen else MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier, horizontalAlignment = alignment) {
        Surface(
            color = labelColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = labelColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Text(
            text = shortName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = fullName,
            style = MaterialTheme.typography.bodySmall,
            color = if (isWinner) winnerGreen.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(8.dp))

        if (showScore) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = contentColor
            )
            if (isWinner) {
                Text(
                    text = "WINNER",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = winnerGreen
                )
            }
        } else {
            Text(
                text = "TBD",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Thin,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
fun StatusLabel(game: GameEntity, isMen: Boolean) {
    val (text, color) = when (game.state) {
        "pre" -> game.startTimeDisplay to MaterialTheme.colorScheme.secondary
        "in" -> {
            val period = getPeriodLabel(game.period, isMen)
            "${game.displayClock} - $period" to Color(0xFFD32F2F)
        }
        "post" -> "FINAL" to MaterialTheme.colorScheme.outline
        else -> game.startTimeDisplay to MaterialTheme.colorScheme.outline
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (game.state == "in") {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 1f, targetValue = 0.3f,
                animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse), label = "alpha"
            )
            Box(Modifier.size(10.dp).clip(CircleShape).background(color.copy(alpha = alpha)))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = color,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun EmptyState(date: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🏀", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text("No games found for", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
            Text(formatDateForDisplay(date), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDatePicker(currentDate: String, onDateSelected: (String) -> Unit) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateStringToMillis(currentDate))
    DatePickerDialog(
        onDismissRequest = { },
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
                    onDateSelected(sdf.format(Date(it)))
                }
            }) { Text("OK") }
        }
    ) { DatePicker(state = datePickerState) }
}

fun getPeriodLabel(period: Int, isMen: Boolean): String {
    return if (isMen) {
        when (period) { 1 -> "1ST HALF"; 2 -> "2ND HALF"; else -> "${period - 2}OT" }
    } else {
        when (period) {
            1 -> "1ST QTR"; 2 -> "2ND QTR"; 3 -> "3RD QTR"; 4 -> "4TH QTR"
            else -> "${period - 4}OT"
        }
    }
}

fun formatDateForDisplay(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val display = SimpleDateFormat("MMM d, yyyy", Locale.US)
        display.format(parser.parse(dateString)!!)
    } catch (e: Exception) { dateString }
}

fun dateStringToMillis(dateString: String): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
        sdf.parse(dateString)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) { System.currentTimeMillis() }
}