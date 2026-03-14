package edu.nd.pmcburne.hwapp.one.network

import com.google.gson.annotations.SerializedName

data class ScoreboardResponse(
    @SerializedName("games") val games: List<GameWrapper>? = null
)

data class GameWrapper(
    @SerializedName("game") val game: Game? = null
)

data class Game(
    @SerializedName("gameID")        val gameID: String = "",
    @SerializedName("away")          val away: TeamData? = null,
    @SerializedName("home")          val home: TeamData? = null,
    @SerializedName("gameState")     val gameState: String = "",   // "final", "live", "pre"
    @SerializedName("startTime")     val startTime: String = "",   // "7:00 PM ET"
    @SerializedName("currentPeriod") val currentPeriod: String = "", // "FINAL", "HALF", "2ND"
    @SerializedName("contestClock")  val contestClock: String = ""  // "14:32"
)

data class TeamData(
    @SerializedName("names")  val names: TeamNames? = null,
    @SerializedName("score")  val score: String = "0",
    @SerializedName("winner") val winner: Boolean = false
)

data class TeamNames(
    @SerializedName("short") val short: String = "",
    @SerializedName("char6") val char6: String = ""
)