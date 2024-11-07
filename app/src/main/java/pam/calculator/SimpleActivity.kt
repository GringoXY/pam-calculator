package pam.calculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.math.BigDecimal
import java.util.Stack

class SimpleActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var txtResult: TextView
    private lateinit var txtHistory: TextView
    private lateinit var scrollViewHistory: ScrollView

    private val lastNumeric: Boolean
        get() = txtResult.text.lastOrNull()?.isDigit() ?: run {
            false
        }

    private val hasValidPrecedenceOperatorForMinus: Boolean
        get() = txtResult.text.lastOrNull() in arrayOf('+', '*', '/')

    private var stateError: Boolean = false

    private val buttons: List<Int> = listOf(
        // Number creation
        R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3,
        R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7,
        R.id.btn_8, R.id.btn_9, R.id.btn_decimal,

        // Operations
        R.id.btn_add, R.id.btn_subtract, R.id.btn_multiply, R.id.btn_divide,

        // Actions
        R.id.btn_all_clear, R.id.btn_clear, R.id.btn_backspace,
        R.id.btn_opposite, R.id.btn_equals
    )

    private val savedResultInstanceStateKey: String = "RESULT_TEXT"
    private val savedHistoryInstanceStateKey: String = "HISTORY_TEXT"
    private val savedStateErrorInstanceStateKey: String = "STATE_ERROR"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.simple)

        findControls()
        setListeners()

        savedInstanceState?.let {
            txtResult.text = it.getString(savedResultInstanceStateKey)
            txtHistory.text = it.getString(savedHistoryInstanceStateKey)
            stateError = it.getBoolean(savedStateErrorInstanceStateKey)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(savedResultInstanceStateKey, txtResult.text.toString())
        outState.putString(savedHistoryInstanceStateKey, txtHistory.text.toString())
        outState.putBoolean(savedStateErrorInstanceStateKey, stateError)
    }

    private fun findControls() {
        txtResult = findViewById(R.id.result)
        txtHistory = findViewById(R.id.history)
        scrollViewHistory = findViewById(R.id.scrollView_history)
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
            R.id.btn_multiply, R.id.btn_divide -> onOperator(view)

            R.id.btn_all_clear -> onAllClear()
            R.id.btn_clear -> onClear()
            R.id.btn_backspace -> onBackspace()
            R.id.btn_opposite -> onOpposite()
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
        val text = txtResult.text
        run breaking@ {
            text.reversed().forEachIndexed() { i, c ->
                if (c.equals('.')) { // Dot already in number. Can't add another!
                    return@breaking
                }

                if (c in arrayOf('+', '-', '/', '*') || (c.isDigit() && i == text.length - 1)) {
                    txtResult.append(".")
                    return@breaking
                }
            }
        }
    }

    private fun onOperator(view: View) {
        if (
            (
                lastNumeric
                || (
                    (hasValidPrecedenceOperatorForMinus || txtResult.text.isEmpty())
                    && (view as Button).text == "-"
                )
            )
            && !stateError
        ) {
            txtResult.append((view as Button).text)
        }
    }

    private fun onAllClear() {
        txtHistory.text = ""
        onClear()
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

    private fun onOpposite() {
        val text = txtResult.text.toString();
        if (text.isNotEmpty()) {
            val lastOperatorIndex = maxOf(
                text.lastIndexOf('+'),
                text.lastIndexOf('-'),
                text.lastIndexOf('*'),
                text.lastIndexOf('/')
            )

            var lastNumber: String = ""
            var leftExpression: String = ""
            if (lastOperatorIndex < 1) { // Single number occurrence
                lastNumber = text
            } else {
                leftExpression = text.substring(0, lastOperatorIndex + 1)
                lastNumber = text.substring(lastOperatorIndex + 1)
            }

            leftExpression = if (
                leftExpression.lastOrNull() == '-'
                && leftExpression.getOrNull(leftExpression.length - 2) in arrayOf('+', '-', '*', '/')
            ) {
                leftExpression.dropLast(1)
            } else if (leftExpression.isEmpty() && lastNumber.firstOrNull() == '-') {
                lastNumber = lastNumber.substring(1)
                leftExpression
            } else {
                "$leftExpression-"
            }

            txtResult.text = "$leftExpression$lastNumber"
        }
    }

    private fun onEqual() {
        if (lastNumeric && !stateError) {
            val text: String = txtResult.text.toString()
            try {
                val result = evaluate(text)
                txtResult.text = result.toPlainString()
                appendToHistory("$text=$result")
            } catch (e: Exception) {
                txtResult.text = "Error: ${e.message}"
                stateError = true
            }
        }
    }

    private fun appendToHistory(line: String) {
        txtHistory.append("\n${line}")
        scrollViewHistory.post {
            scrollViewHistory.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun evaluate(expression: String): BigDecimal  {
        val tokens = expression.toCharArray()
        val values = Stack<BigDecimal>()
        val operations = Stack<Char>()

        var i = 0
        while (i < tokens.size) {
            when {
                (
                    tokens[i].isDigit()
                    || tokens[i] == '.'
                ) || (
                    tokens[i] == '-'
                    && (
                        i == 0 // When '-' is before first number
                        || tokens[i - 1] in arrayOf('+', '-', '*', '/') // When '-' before operator
                    )
                ) -> {
                    val numberSb = StringBuilder()
                    if (tokens[i] == '-') {
                        numberSb.append(tokens[i++])
                    }

                    while (i < tokens.size && (tokens[i].isDigit() || tokens[i] == '.')) {
                        numberSb.append(tokens[i++])
                    }

                    values.push(numberSb.toString().toBigDecimal())
                    i -= 1
                }
                tokens[i] in arrayOf('+', '-', '*', '/') -> {
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
            (op1 == '*' || op1 == '/')
            && (op2 == '+' || op2 == '-')
        ) {
            return false
        }

        return true
    }

    private fun applyOp(op: Char, b: BigDecimal, a: BigDecimal): BigDecimal {
        return when (op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> {
                if (b == 0.0.toBigDecimal()) {
                    throw ArithmeticException("Cannot divide by zero")
                }

                a / b
            }
            else -> 0.0.toBigDecimal()
        }
    }
}