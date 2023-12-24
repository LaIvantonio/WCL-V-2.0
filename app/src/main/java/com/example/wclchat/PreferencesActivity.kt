import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.wclchat.MainViewModel
import com.example.wclchat.Preferences
import com.example.wclchat.databinding.ActivityPreferencesBinding

class PreferencesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreferencesBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSavePreferences.setOnClickListener {
            savePreferences()
        }
    }

    private fun savePreferences() {
        val preferences = Preferences(
            monuments = binding.checkboxMonuments.isChecked,
            museums = binding.checkboxMuseums.isChecked,
            parks = binding.checkboxParks.isChecked,
            theaters = binding.checkboxTheaters.isChecked
            // Добавь другие категории по желанию
        )
        viewModel.savePreferences(preferences)
    }
}