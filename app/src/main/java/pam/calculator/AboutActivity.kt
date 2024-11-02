package pam.calculator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {
    private lateinit var btnProfile: Button

    private val githubProfileUri: String = "https://github.com/GringoXY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.about)

        findControls()
        setOnClickListeners();
    }

    fun findControls() {
        btnProfile = findViewById<Button>(R.id.btnProfile)
    }

    fun setOnClickListeners() {
        btnProfile.setOnClickListener {
            val openUrl = Intent(Intent.ACTION_VIEW)
            openUrl.data = Uri.parse(githubProfileUri)
            startActivity(openUrl)
        }
    }

}
