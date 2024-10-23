package pam.calculator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var btnSimple: Button;

    private val menuMap: Map<Int, Class<*>> = mapOf(
        R.layout.simple to SimpleActivity::class.java
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.menu)

        findControls()
        setOnClickListeners();
    }

    fun findControls() {
        btnSimple = findViewById<Button>(R.id.btnSimple)
    }

    fun setOnClickListeners() {
        btnSimple.setOnClickListener {
            menuMap.get(R.layout.simple)?.let { c -> onMenuButtonClick(c) }
        }
    }

    fun onMenuButtonClick(c: Class<*>) {
        startActivity(Intent(this, c))
    }
}