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
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class AdvancedActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var txtResult: TextView
    private lateinit var txtHistory: TextView
    private lateinit var scrollViewHistory: ScrollView

    private val operatorsMap = mapOf<Int, CharSequence>(
        R.id.btn_add to "+",
        R.id.btn_subtract to "-",
        R.id.btn_multiply to "*",
        R.id.btn_divide to "/",
        R.id.btn_power to "^"
    )

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
        R.id.btn_sin, R.id.btn_cos, R.id.btn_tan,
        R.id.btn_sqrt, R.id.btn_power_two, R.id.btn_power,
        R.id.btn_ln, R.id.btn_log,

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
        setContentView(R.layout.advanced)

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
            R.id.btn_multiply, R.id.btn_divide, R.id.btn_power -> onOperator(view)

            R.id.btn_sin -> onSinus()
            R.id.btn_cos -> onCosine()
            R.id.btn_sqrt -> onSquareRoot()
            R.id.btn_tan -> onTangent()
            R.id.btn_power_two -> onPowerTwo()
            R.id.btn_ln -> onNaturalLogarithm()
            R.id.btn_log -> onCommonLogarithm()

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

                if (c in arrayOf('+', '-', '/', '*', '^') || (c.isDigit() && i == text.length - 1)) {
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
                    && view.id == R.id.btn_subtract
                )
            )
            && !stateError
        ) {
            txtResult.append(operatorsMap.get(view.id))
        }
    }

    private fun onSinus() {
        if (lastNumeric && !stateError) {
            val text: String = txtResult.text.toString()
            try {
                val result = evaluate(text)
                val radians = Math.toRadians(result.toDouble())
                val sinus = sin(radians).toString()
                txtResult.text = sinus
                appendToHistory("sin($radians rad)=$sinus")
            } catch (e: Exception) {
                txtResult.text = "Error: ${e.message}"
                stateError = true
            }
        }
    }

    private fun onCosine() {
        if (lastNumeric && !stateError) {
            val text: String = txtResult.text.toString()
            try {
                val result = evaluate(text)
                val radians = Math.toRadians(result.toDouble())
                val cosine = cos(radians).toString()
                txtResult.text = cosine
                appendToHistory("cos($radians rad)=$cosine")
            } catch (e: Exception) {
                txtResult.text = "Error: ${e.message}"
                stateError = true
            }
        }
    }

    private fun onSquareRoot() {
        if (lastNumeric && !stateError) {
            val text: String = txtResult.text.toString()
            try {
                val result = evaluate(text)
                val sqrt = sqrt(result.toDouble()).toString()
                txtResult.text = sqrt
                appendToHistory("sqrt($result)=$sqrt")
            } catch (e: Exception) {
                txtResult.text = "Error: ${e.message}"
                stateError = true
            }
        }
    }

    private fun onTangent() {
        if (lastNumeric && !stateError) {
            val text: String = txtResult.text.toString()
            try {
                val result = evaluate(text)
                val radians = Math.toRadians(result.toDouble())
                val tangent = tan(radians).toString()
                txtResult.text = tangent
                appendToHistory("tan($radians rad)=$tangent")
            } catch (e: Exception) {
                txtResult.text = "Error: ${e.message}"
                stateError = true
            }
        }
    }

    private fun onPowerTwo() {
        if (lastNumeric && !stateError) {
            val text: String = txtResult.text.toString()
            try {
                val result = evaluate(text)
                val power = result.pow(2).toString()
                txtResult.text = power
                appendToHistory("($text)^2=$power")
            } catch (e: Exception) {
                txtResult.text = "Error: ${e.message}"
                stateError = true
            }
        }
    }

    private fun onNaturalLogarithm() {
        if (lastNumeric && !stateError) {
            val text: String = txtResult.text.toString()
            try {
                val result = evaluate(text)
                val naturalLogarithm = ln(result.toDouble()).toString()
                txtResult.text = naturalLogarithm
                appendToHistory("ln($text)=$naturalLogarithm")
            } catch (e: Exception) {
                txtResult.text = "Error: ${e.message}"
                stateError = true
            }
        }
    }

    private fun onCommonLogarithm() {
        if (lastNumeric && !stateError) {
            val text: String = txtResult.text.toString()
            try {
                val result = evaluate(text)
                val commonLogarithm = log10(result.toDouble()).toString()
                txtResult.text = commonLogarithm
                appendToHistory("log($text)=$commonLogarithm")
            } catch (e: Exception) {
                txtResult.text = "Error: ${e.message}"
                stateError = true
            }
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
                text.lastIndexOf('/'),
                text.lastIndexOf('^')
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
                && leftExpression.getOrNull(leftExpression.length - 2) in arrayOf('+', '-', '*', '/', '^')
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
                        || tokens[i - 1] in arrayOf('+', '-', '*', '/', '^') // When '-' before operator
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
                tokens[i] in arrayOf('+', '-', '*', '/', '^') -> {
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
            (op1 == '*' || op1 == '/' || op1 == '^')
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
            '^' -> {
                val bRemainder = b.remainder(BigDecimal.ONE)
                val bInt = b.subtract(bRemainder)

                a.pow(bInt.intValueExact()) * BigDecimal(a.toDouble().pow(bRemainder.toDouble()))
            }
            else -> 0.0.toBigDecimal()
        }
    }
}