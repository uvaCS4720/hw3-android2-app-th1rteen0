package edu.nd.pmcburne.hwapp.one.database

import android.content.Context
import androidx.room.*

// room database: stores games locally so the app works offline
// @Entity, the table (one row = one game)
// @Dao, the functions for reading/writing that table
// @Database, ties it together and gives a database instance


// each field becomes a colum,  @PrimaryKey uniquely identifies each row
@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey
    val uid: String, // build this as "$gender-$gameId" so it's always unique

    val gameId: String,
    val gender: String, // "men" or "women"
    val date: String, // "yyyy-mm-dd"

    val homeTeamName: String,
    val awayTeamName: String,
    val homeTeamShort: String,
    val awayTeamShort: String,

    val homeScore: Int,
    val awayScore: Int,

    val state: String, // "pre", "in", or "post"
    val startTimeDisplay: String, //e.g. "7:00 PM ET" — shown for upcoming games
    val displayClock: String, // e.g. "14:32" — shown for live games
    val period: Int, // which half or quarter we're in

    val homeWinner: Boolean,
    val awayWinner: Boolean
)


// read from and write to the database
@Dao
interface GameDao {
    // get all games for a specific gender and date
    // sort them so live games appear first, then finished, then upcoming
    @Query("""
        SELECT * FROM games 
        WHERE gender = :gender AND date = :date
        ORDER BY 
            CASE state 
                WHEN 'in'   THEN 0
                WHEN 'post' THEN 1
                ELSE             2
            END
    """)
    suspend fun getGames(gender: String, date: String): List<GameEntity>

    // insert or update a game, if a game with the same uid already exists, it gets replaced with the new data (so live scores stay current)
    @Upsert
    suspend fun upsertGames(games: List<GameEntity>)
}


// singleton database, only open one database connection for the whole app
@Database(entities = [GameEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if instance already exist, return it
            // if not, create one, synchronized block prevents two threads from creating the database at the same time
            return instance ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "basketball_scores.db"
                ).build()
                instance = db
                db
            }
        }
    }
}