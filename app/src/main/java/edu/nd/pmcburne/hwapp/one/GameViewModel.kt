package edu.nd.pmcburne.hwapp.one

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.nd.pmcburne.hwapp.one.database.AppDatabase
import edu.nd.pmcburne.hwapp.one.database.GameEntity
import edu.nd.pmcburne.hwapp.one.network.ApiClient
import edu.nd.pmcburne.hwapp.one.network.Game
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class GameViewModel(application: Application) : AndroidViewModel(application) {

    // reference to database DAO
    private val gameDao = AppDatabase.getDatabase(application).gameDao()

    // mutableStateOf = automatically re-draw when changes occur

    var games by mutableStateOf<List<GameEntity>>(emptyList())
        private set   // only this viewmodel can change these values

    var isLoading by mutableStateOf(false)
        private set

    var isOffline by mutableStateOf(false)
        private set

    // "men" or "women"
    var selectedGender by mutableStateOf("men")
        private set

    // date stored as "yyyy-mm-dd", defaults to today
    var selectedDate by mutableStateOf(todayAsString())
        private set

    // run a fetch when the viewmodel is first created
    init {
        loadGames()
    }

    // called when the user picks a new date from the date picker
    fun onDateSelected(newDate: String) {
        selectedDate = newDate
        loadGames()
    }

    // called when the user taps the gender toggle
    fun onGenderSelected(gender: String) {
        selectedGender = gender
        loadGames()
    }

    // called when the user taps refresh
    fun onRefresh() {
        loadGames()
    }

    // main function that fetches games
    // runs in a coroutine (viewModelScope.launch) so it doesn't block the UI
    private fun loadGames() {
        viewModelScope.launch {
            isLoading = true
            android.util.Log.d("GameViewModel", "Network available: ${isNetworkAvailable()}")

            if (isNetworkAvailable()) {
                isOffline = false
                try {
                    val parts = selectedDate.split("-")
                    val response = ApiClient.service.getScoreboard(selectedGender, parts[0], parts[1], parts[2])

                    android.util.Log.d("GameViewModel", "API responded, games count: ${response.games?.size}")

                    val entities = (response.games ?: emptyList()).mapNotNull { wrapper ->
                        convertGameToEntity(wrapper.game, selectedGender, selectedDate)
                    }

                    android.util.Log.d("GameViewModel", "Entities converted: ${entities.size}")

                    gameDao.upsertGames(entities)

                } catch (e: Exception) {
                    android.util.Log.e("GameViewModel", "API call failed: ${e.message}")
                    e.printStackTrace()
                }
            } else {
                isOffline = true
            }

            games = gameDao.getGames(selectedGender, selectedDate)

            android.util.Log.d("GameViewModel", "Games loaded from DB: ${games.size}")

            isLoading = false
        }
    }

    // converts one API event object into a gameevent for the database
    // returns null if the event data is missing required fields.
    private fun convertGameToEntity(game: Game?, gender: String, date: String): GameEntity? {
        if (game == null) return null

        val state = when (game.gameState.lowercase()) {
            "final" -> "post"
            "live"  -> "in"
            else    -> "pre"
        }

        return GameEntity(
            uid              = "$gender-${game.gameID}",
            gameId           = game.gameID,
            gender           = gender,
            date             = date,
            homeTeamName     = game.home?.names?.short ?: "Home Team",
            awayTeamName     = game.away?.names?.short ?: "Away Team",
            homeTeamShort    = game.home?.names?.char6 ?: "HOME",
            awayTeamShort    = game.away?.names?.char6 ?: "AWAY",
            homeScore        = game.home?.score?.toIntOrNull() ?: 0,
            awayScore        = game.away?.score?.toIntOrNull() ?: 0,
            state            = state,
            startTimeDisplay = game.startTime,
            displayClock     = game.contestClock,
            period           = parsePeriod(game.currentPeriod, gender),
            homeWinner       = game.home?.winner ?: false,
            awayWinner       = game.away?.winner ?: false
        )
    }

    // converts "1ST", "2ND", "1ST QTR" etc. into a period number
    private fun parsePeriod(currentPeriod: String, gender: String): Int {
        return when {
            currentPeriod.contains("1ST") -> 1
            currentPeriod.contains("2ND") && gender == "men" -> 2
            currentPeriod.contains("2ND") -> 2
            currentPeriod.contains("3RD") -> 3
            currentPeriod.contains("4TH") -> 4
            currentPeriod == "HALF"       -> 1
            else -> 0
        }
    }

    // checks if the device currently has internet access
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getApplication<Application>().getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // helper: returns today's date as "yyyy-mm-dd"
    companion object {
        fun todayAsString(): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        }
    }
}