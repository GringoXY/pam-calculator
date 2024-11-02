package pam.calculator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {
    private lateinit var btnProfile: Button
    private lateinit var btnRepo: Button

    private val githubProfileUri: String = "https://github.com/GringoXY"
    private val githubRepositoryUri: String = "https://github.com/GringoXY/pam-calculator"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.about)

        findControls()
        setOnClickListeners();
    }

    fun findControls() {
        btnProfile = findViewById<Button>(R.id.btnProfile)
        btnRepo = findViewById<Button>(R.id.btnRepo)
    }

    fun setOnClickListeners() {
        btnProfile.setOnClickListener {
            openUrl(githubProfileUri)
        }

        btnRepo.setOnClickListener {
            openUrl(githubRepositoryUri)
        }
    }

    private fun openUrl(uri: String) {
        val openUrl = Intent(Intent.ACTION_VIEW)
        openUrl.data = Uri.parse(uri)
        startActivity(openUrl)
    }

}
