package pam.calculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.util.Stack

class SimpleActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var txtResult: TextView

    private val lastNumeric: Boolean
        get() = txtResult.text.last().isDigit()

    private var stateError: Boolean = false

    private val lastDot: Boolean
        get() = txtResult.text.last() == '.'

    private val buttons: List<Int> = listOf(
        // Number creation
        R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3,
        R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7,
        R.id.btn_8, R.id.btn_9, R.id.btn_decimal,

        // Operations
        R.id.btn_add, R.id.btn_subtract, R.id.btn_multiply, R.id.btn_divide,

        // Actions
        R.id.btn_percentage, R.id.btn_clear, R.id.btn_backspace, R.id.btn_equals
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.simple)

        txtResult = findViewById(R.id.result)
        setListeners()
    }

    private fun setListeners() {
        for (id in buttons) {
            findViewById<Button>(id).setOnClickListener(this)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3,
            R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7,
            R.id.btn_8, R.id.btn_9 -> onDigit(view)
            R.id.btn_decimal -> onDecimalPoint()
            R.id.btn_add, R.id.btn_subtract,
            R.id.btn_multiply, R.id.btn_divide,
            R.id.btn_percentage -> onOperator(view)
            R.id.btn_clear -> onClear()
            R.id.btn_backspace -> onBackspace()
            R.id.btn_equals -> onEqual()
        }
    }

    private fun onDigit(view: View) {
        val text: CharSequence = (view as Button).text
        if (stateError) {
            txtResult.text = text
            stateError = false
        } else {
            txtResult.append(text)
        }
    }

    private fun onDecimalPoint() {
        if (lastNumeric && !lastDot) {
            txtResult.append(".")
        }
    }

    private fun onOperator(view: View) {
        if (lastNumeric && !stateError) {
            txtResult.append((view as Button).text)
        }
    }

    private fun onClear() {
        stateError = false
        txtResult.text = ""
    }

    private fun onBackspace() {
        val text: String = txtResult.text.toString()
        if (text.isNotEmpty()) {
            txtResult.text = text.dropLast(1)
        }
    }

    private fun onEqual() {
        if (lastNumeric && !stateError) {
            val text: String = txtResult.text.toString()
            try {
                val result = evaluate(text)
                txtResult.text = result.toString()
            } catch (e: Exception) {
                txtResult.text = "Error: ${e.message}"
                stateError = true
            }
        }
    }

    private fun evaluate(expression: String): Double  {
        val tokens = expression.toCharArray()
        val values = Stack<Double>()
        val operations = Stack<Char>()

        var i = 0
        while (i < tokens.size) {
            when {
                tokens[i].isDigit() || tokens[i] == '.' -> {
                    val sb = StringBuilder()
                    while (i < tokens.size && (tokens[i].isDigit() || tokens[i] == '.')) {
                        sb.append(tokens[i++])
                    }

                    values.push(sb.toString().toDouble())
                    i -= 1
                }
                tokens[i] in arrayOf('+', '-', '*', '/', '%') -> {
                    while (operations.isNotEmpty() && hasPrecedence(tokens[i], operations.peek())) {
                        values.push(applyOp(operations.pop(), values.pop(), values.pop()))
                    }

                    operations.push(tokens[i])
                }
            }

            i += 1
        }

        while (operations.isNotEmpty()){
            values.push(applyOp(operations.pop(), values.pop(), values.pop()))
        }

        return values.pop()
    }

    private fun hasPrecedence(op1: Char, op2: Char): Boolean {
        if (
            (op1 == '*' || op1 == '/' || op1 == '%')
            && (op2 == '+' || op2 == '-')
        ) {
            return false
        }

        return true
    }

    private fun applyOp(op: Char, b: Double, a: Double): Double {
        return when (op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> {
                if (b == 0.0) throw ArithmeticException("Cannot divide by zero")
                a / b
            }
            '%' -> a % b
            else -> 0.0
        }
    }
}