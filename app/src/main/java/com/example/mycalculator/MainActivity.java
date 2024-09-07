package com.example.mycalculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Stack;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private TextView inputTextView;
    private TextView resultTextView;
    private StringBuilder currentInput = new StringBuilder();
    private boolean operatorPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        inputTextView = findViewById(R.id.operationTextView);
        resultTextView = findViewById(R.id.resultTextView);

        setupNumberButtons();
        setupOperatorButtons();
        setupClearButton();
        setupEqualsButton();
        setupDeleteButton();
        setupDecimalButton();
    }

    // Method to setup the decimal point button
    private void setupDecimalButton() {
        Button decimalButton = findViewById(R.id.buttonDot);
        decimalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (operatorPressed) {
                    currentInput.setLength(0);
                    operatorPressed = false;
                }
                // Prevent adding multiple decimal points in one number
                if (currentInput.length() > 0 && currentInput.charAt(currentInput.length() - 1) != '.') {
                    currentInput.append('.');
                    inputTextView.setText(currentInput.toString());
                }
            }
        });
    }

    private void setupNumberButtons() {
        int[] numberButtonIds = {
                R.id.button0, R.id.button1, R.id.button2, R.id.button3,
                R.id.button4, R.id.button5, R.id.button6, R.id.button7,
                R.id.button8, R.id.button9
        };

        View.OnClickListener numberClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                String number = button.getText().toString();
                if (operatorPressed) {
                    currentInput.setLength(0);
                    operatorPressed = false;
                }
                currentInput.append(number);
                inputTextView.setText(currentInput.toString());
            }
        };

        for (int id : numberButtonIds) {
            findViewById(id).setOnClickListener(numberClickListener);
        }
    }

    private void setupOperatorButtons() {
        int[] operatorButtonIds = {
                R.id.buttonAdd, R.id.buttonSubtract, R.id.buttonMultiply, R.id.buttonDivide,
                R.id.buttonParenthesesOpen, R.id.buttonParenthesesClose
        };

        View.OnClickListener operatorClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                char newOperator = button.getText().toString().charAt(0);

                if (newOperator == '(') {
                    currentInput.append(newOperator);
                } else if (newOperator == ')') {
                    currentInput.append(newOperator);
                } else {
                    if (operatorPressed && currentInput.length() > 0) {
                        currentInput.append(" ");
                    }
                    currentInput.append(newOperator);
                }
                inputTextView.setText(currentInput.toString());
                operatorPressed = false;
            }
        };

        for (int id : operatorButtonIds) {
            findViewById(id).setOnClickListener(operatorClickListener);
        }
    }

    private void setupClearButton() {
        Button clearButton = findViewById(R.id.buttonC);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentInput.setLength(0);
                inputTextView.setText("");
                resultTextView.setText("0");
            }
        });
    }

    private void setupDeleteButton() {
        Button deleteButton = findViewById(R.id.buttonAC);  // Pastikan ini tombol untuk "AC"
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentInput.length() > 0) {
                    currentInput.deleteCharAt(currentInput.length() - 1);
                    inputTextView.setText(currentInput.toString());
                }
            }
        });
    }

    private void setupEqualsButton() {
        Button equalsButton = findViewById(R.id.buttonEquals);
        equalsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentInput.length() == 0) return;

                String infixExpression = currentInput.toString();
                List<String> postfixExpression = infixToPostfix(infixExpression);
                double result = evaluatePostfix(postfixExpression);

                inputTextView.setText("");
                resultTextView.setText(String.valueOf(result));
                currentInput.setLength(0);
            }
        });
    }

    private List<String> infixToPostfix(String infix) {
        List<String> output = new ArrayList<>();
        Stack<Character> operators = new Stack<>();
        StringBuilder number = new StringBuilder();

        for (int i = 0; i < infix.length(); i++) {
            char c = infix.charAt(i);

            if (Character.isDigit(c) || c == '.') {
                number.append(c);
            } else {
                if (number.length() > 0) {
                    output.add(number.toString());
                    number.setLength(0);
                }

                if (c == '(') {
                    operators.push(c);
                } else if (c == ')') {
                    while (!operators.isEmpty() && operators.peek() != '(') {
                        output.add(String.valueOf(operators.pop()));
                    }
                    operators.pop(); // Remove '('
                } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                    while (!operators.isEmpty() && precedence(c) <= precedence(operators.peek())) {
                        output.add(String.valueOf(operators.pop()));
                    }
                    operators.push(c);
                }
            }
        }

        // Append the last number if there is one
        if (number.length() > 0) {
            output.add(number.toString());
        }

        while (!operators.isEmpty()) {
            output.add(String.valueOf(operators.pop()));
        }

        return output;
    }

    private double evaluatePostfix(List<String> postfix) {
        Stack<Double> stack = new Stack<>();

        for (String token : postfix) {
            if (isNumeric(token)) {
                stack.push(Double.parseDouble(token));
            } else {
                double right = stack.pop();
                double left = stack.pop();
                switch (token.charAt(0)) {
                    case '+':
                        stack.push(left + right);
                        break;
                    case '-':
                        stack.push(left - right);
                        break;
                    case '*':
                        stack.push(left * right);
                        break;
                    case '/':
                        if (right != 0) {
                            stack.push(left / right);
                        } else {
                            resultTextView.setText("Error");
                            return 0;
                        }
                        break;
                }
            }
        }

        return stack.isEmpty() ? 0 : stack.pop();
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private int precedence(char operator) {
        switch (operator) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
            default:
                return 0;
        }
    }
}
