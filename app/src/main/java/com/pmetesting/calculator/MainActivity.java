package com.pmetesting.calculator;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private StringBuilder currentInput;
    private TextView textViewInput;
    private boolean isError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentInput = new StringBuilder();
        textViewInput = findViewById(R.id.textView_input_numbers);

        setNumberButtonListeners();
        setOperatorButtonListeners();
        setOtherButtonListeners();
    }

    private void setNumberButtonListeners() {
        int[] numberButtonIds = {
                R.id.button_zero, R.id.button_one, R.id.button_two, R.id.button_three,
                R.id.button_four, R.id.button_five, R.id.button_six, R.id.button_seven,
                R.id.button_eight, R.id.button_nine
        };

        for (int buttonId : numberButtonIds) {
            Button button = findViewById(buttonId);
            button.setOnClickListener(view -> appendToInput(button.getText().toString()));
        }
    }

    private void setOperatorButtonListeners() {
        int[] operatorButtonIds = {
                R.id.button_addition, R.id.button_subtraction,
                R.id.button_multiplication, R.id.button_division
        };

        for (int buttonId : operatorButtonIds) {
            Button button = findViewById(buttonId);
            button.setOnClickListener(view -> {
                if (!isError) {
                    appendToInput(" " + button.getText().toString() + " ");
                }
            });
        }
    }

    private void setOtherButtonListeners() {
        Button buttonDot = findViewById(R.id.button_dot);
        Button buttonEqual = findViewById(R.id.button_equal);
        Button buttonClear = findViewById(R.id.button_clear);

        buttonDot.setOnClickListener(view -> {
            if (!isError) {
                String input = textViewInput.getText().toString();
                if (input.isEmpty() || isLastCharOperator(input)) {
                    appendToInput("0.");
                } else if (!input.contains(".")) {
                    appendToInput(".");
                }
            }
        });

        buttonEqual.setOnClickListener(view -> calculateResult());

        buttonClear.setOnClickListener(view -> clearInput());
    }

    private void appendToInput(String value) {
        if (isError) {
            clearInput();
        }
        currentInput.append(value);
        updateInputTextView();
    }

    private void updateInputTextView() {
        textViewInput.setText(currentInput.toString());
    }

    private boolean isLastCharOperator(String input) {
        char lastChar = input.charAt(input.length() - 1);
        return lastChar == '+' || lastChar == '-' || lastChar == 'x' || lastChar == '\u00F7';
    }

    private void calculateResult() {
        try {
            String expression = currentInput.toString();
            double result = eval(expression);

            String formattedResult = (result % 1 == 0) ? String.valueOf((int) result) : String.valueOf(result);

            currentInput.setLength(0);
            currentInput.append(formattedResult);
            updateInputTextView();
        } catch (Exception e) {
            currentInput.setLength(0);
            currentInput.append("Error");
            updateInputTextView();
            isError = true;
        }
    }


    private void clearInput() {
        currentInput.setLength(0);
        updateInputTextView();
        isError = false;
    }

    private double eval(String expression) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (Character.isWhitespace(ch)) nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('x')) x *= parseFactor();
                    else if (eat('\u00F7')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if (Character.isDigit(ch) || ch == '.') {
                    while (Character.isDigit(ch) || ch == '.') nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                return x;
            }
        }.parse();
    }
}
