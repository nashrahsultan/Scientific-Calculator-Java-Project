package Calculator;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.EmptyStackException;
import java.util.Stack;
import java.lang.Math;

public class ScientificCalculator {
    private JFrame frame;
    private JTextField display;
    private String input = "";
    private Stack<Double> memory = new Stack<>();

    public ScientificCalculator() {
        frame = new JFrame("Scientific Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout());

        display = new JTextField();
        display.setFont(new Font("Arial", Font.PLAIN, 20));
        display.setEditable(false);
        frame.add(display, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(6, 5));

        String[] buttons = {
                "7", "8", "9", "/", "sqrt",
                "4", "5", "6", "*", "x^2",
                "1", "2", "3", "-", "Backspace",
                "0", ".", "+", "=",
                "sin", "cos", "tan", "log", "ln",
                "Clear"
        };

        for (String button : buttons) {
            JButton btn = new JButton(button);
            btn.setFont(new Font("Calibri", Font.PLAIN, 18));
            btn.addActionListener(new ButtonClickListener());
            buttonPanel.add(btn);
        }

        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            if ("0123456789.".contains(command)) {
                input += command;
            } else if ("+-*/".contains(command)) {
                input += " " + command + " ";
            } else if ("sqrt x^2 sin cos tan log ln".contains(command)) {
                input = command + "(" + input + ")";
            } else if ("Backspace".equals(command)) { // Handle Backspace button
                if (!input.isEmpty()) {
                    input = input.substring(0, input.length() - 1);
                }
            } else if ("=".equals(command)) {
                try {
                    input = evaluateExpression(input);
                } catch (ArithmeticException ex) {
                    input = "Error: " + ex.getMessage();
                }
            } else if ("Clear".equals(command)) {
                input = "";
            }

            display.setText(input);
        }

        private String evaluateExpression(String expression) {
            String result = "";
            try {
                // Add spaces around parentheses and operators to facilitate splitting
                expression = expression.replaceAll("([()+\\-*/])", " $1 ");
                String[] parts = expression.split("\\s+"); // Split by whitespace
                Stack<Double> values = new Stack<>();
                Stack<String> operators = new Stack<>();

                for (String part : parts) {
                    if (isNumeric(part)) {
                        values.push(Double.parseDouble(part));
                    } else if (isOperator(part)) {
                        while (!operators.isEmpty() && precedence(part) <= precedence(operators.peek())) {
                            applyOperator(values, operators);
                        }
                        operators.push(part);
                    } else if (part.equals("(")) {
                        operators.push(part);
                    } else if (part.equals(")")) {
                        // Evaluate the expression inside the parentheses
                        while (!operators.peek().equals("(")) {
                            applyOperator(values, operators);
                        }
                        operators.pop(); // Remove "("

                        // If the next operator is "sqrt" or "x^2", apply it to the value
                        if (!operators.isEmpty()
                                && (operators.peek().equals("sqrt") || operators.peek().equals("x^2"))) {
                            String op = operators.pop();
                            double value = values.pop();
                            double opResult = op.equals("sqrt") ? Math.sqrt(value) : Math.pow(value, 2);
                            values.push(opResult);
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid input: " + part);
                    }
                }

                // Apply remaining operators
                while (!operators.isEmpty()) {
                    applyOperator(values, operators);
                }

                DecimalFormat df = new DecimalFormat("#.##########");
                result = df.format(values.pop());
            } catch (NumberFormatException | EmptyStackException | ArithmeticException e) {
                result = "Error: " + e.getMessage();
            }
            return result;
        }

        private boolean isNumeric(String str) {
            return str.matches("-?\\d+(\\.\\d+)?");
        }

        private boolean isOperator(String str) {
            return str.matches("[+\\-*/]|sqrt|x\\^2|sin|cos|tan|log|ln");
        }

        private int precedence(String operator) {
            switch (operator) {
                case "+":
                case "-":
                    return 1;
                case "*":
                case "/":
                    return 2;
                default:
                    return 0;
            }
        }

        private void applyOperator(Stack<Double> values, Stack<String> operators) {
            String operator = operators.pop();
            double b = values.pop();
            double a = values.pop();
            double res = calculate(a, b, operator);
            values.push(res);
        }

        private double calculate(double a, double b, String operator) {
            switch (operator) {
                case "+":
                    return a + b;
                case "-":
                    return a - b;
                case "*":
                    return a * b;
                case "/":
                    if (b == 0)
                        throw new ArithmeticException("Division by zero");
                    return a / b;
                case "sqrt":
                    if (a < 0)
                        throw new ArithmeticException("Square root of negative number");
                    return Math.sqrt(a);
                case "x^2":
                    return Math.pow(a, 2);
                case "sin":
                    return Math.sin(Math.toRadians(a));
                case "cos":
                    return Math.cos(Math.toRadians(a));
                case "tan":
                    return Math.tan(Math.toRadians(a));
                case "log":
                    if (a <= 0 || b <= 0 || a == 1)
                        throw new ArithmeticException("Invalid logarithm");
                    return Math.log(b) / Math.log(a);
                case "ln":
                    if (a <= 0)
                        throw new ArithmeticException("Invalid natural logarithm");
                    return Math.log(a);
                default:
                    throw new IllegalArgumentException("Invalid operator: " + operator);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ScientificCalculator::new);
    }
}
