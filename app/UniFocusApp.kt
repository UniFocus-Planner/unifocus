import android.app.Application
import com.example.unifocus.data.database.UniFocusDatabase
import com.example.unifocus.data.repository.UniFocusRepository

class UniFocusApp : Application() {
    lateinit var repository: UniFocusRepository

    override fun onCreate() {
        super.onCreate()
        val database = UniFocusDatabase.getDatabase(this)
        repository = UniFocusRepository(database)
    }
}