package com.liseasons.climate;

import java.math.BigDecimal;
import java.util.Map;

public final class TemperatureFormulaEvaluator {
    public double evaluate(String formula, Map<String, Double> values) {
        String expression = formula;
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            expression = expression.replace("%" + entry.getKey() + "%", toFormulaNumber(entry.getValue()));
        }
        return new Parser(expression).parse();
    }

    private String toFormulaNumber(double value) {
        return BigDecimal.valueOf(value).toPlainString();
    }

    private static final class Parser {
        private final String input;
        private int index;

        private Parser(String input) {
            this.input = input.replace(" ", "");
        }

        private double parse() {
            double value = parseExpression();
            if (this.index < this.input.length()) {
                throw new IllegalArgumentException("无法解析体温公式: " + this.input);
            }
            return value;
        }

        private double parseExpression() {
            double value = parseTerm();
            while (this.index < this.input.length()) {
                char current = this.input.charAt(this.index);
                if (current == '+') {
                    this.index++;
                    value += parseTerm();
                    continue;
                }
                if (current == '-') {
                    this.index++;
                    value -= parseTerm();
                    continue;
                }
                break;
            }
            return value;
        }

        private double parseTerm() {
            double value = parseFactor();
            while (this.index < this.input.length()) {
                char current = this.input.charAt(this.index);
                if (current == '*') {
                    this.index++;
                    value *= parseFactor();
                    continue;
                }
                if (current == '/') {
                    this.index++;
                    value /= parseFactor();
                    continue;
                }
                break;
            }
            return value;
        }

        private double parseFactor() {
            if (this.index >= this.input.length()) {
                throw new IllegalArgumentException("体温公式提前结束: " + this.input);
            }

            char current = this.input.charAt(this.index);
            if (current == '+') {
                this.index++;
                return parseFactor();
            }
            if (current == '-') {
                this.index++;
                return -parseFactor();
            }
            if (current == '(') {
                this.index++;
                double value = parseExpression();
                expect(')');
                return value;
            }

            int start = this.index;
            readNumber();
            if (start == this.index) {
                throw new IllegalArgumentException("体温公式存在无法识别的字符: " + this.input);
            }
            return Double.parseDouble(this.input.substring(start, this.index));
        }

        private void readNumber() {
            boolean hasExponent = false;
            while (this.index < this.input.length()) {
                char current = this.input.charAt(this.index);
                if ((current >= '0' && current <= '9') || current == '.') {
                    this.index++;
                    continue;
                }
                if ((current == 'e' || current == 'E') && !hasExponent) {
                    hasExponent = true;
                    this.index++;
                    if (this.index < this.input.length()) {
                        char sign = this.input.charAt(this.index);
                        if (sign == '+' || sign == '-') {
                            this.index++;
                        }
                    }
                    continue;
                }
                break;
            }
        }

        private void expect(char target) {
            if (this.index >= this.input.length() || this.input.charAt(this.index) != target) {
                throw new IllegalArgumentException("体温公式括号不匹配: " + this.input);
            }
            this.index++;
        }
    }
}
