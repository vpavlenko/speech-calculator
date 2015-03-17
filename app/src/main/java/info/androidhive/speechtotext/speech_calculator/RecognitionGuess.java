package info.androidhive.speechtotext.speech_calculator;

import com.google.common.base.Joiner;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecognitionGuess {

    private static Map<String, String> UNARY_OPERATIONS = new HashMap<String, String>() {{
        put("root", "sqrt");
        put("logarithm", "log");
        put("sign", "sin");
        put("sine", "sin");
        put("cosine", "cos");
        put("tangent", "tan");
    }};

    private static Evaluator mEvaluator = new Evaluator();

    private String recognitionOutput;
    private String mathExpression;
    private String evaluatedValue;
    private boolean isEvaluated;

    RecognitionGuess(String recognitionOutput) {
        this.recognitionOutput = recognitionOutput;
        this.mathExpression = convertToMathExpression(this.recognitionOutput);
        evaluate(this.mathExpression);
    }

    private static String normalizeSingleToken(String token) {
        token = token.toLowerCase();
        if (token.matches("[-+]?[0-9]*\\.?[0-9]*") ) {
            return token;
        } else if (token.equals("times") || token.contains("множ")) {
            return "*";
        } else if (token.startsWith("divi") || token.startsWith("over") || token.contains("дел")) {
            return "/";
        } else if (token.startsWith("plus") || token.contains("плюс")) {
            return "+";
        } else if (token.startsWith("minus") || token.contains("минус")) {
            return "-";
        } else {
            return "";
        }
    }

    private static List<String> normalizeTokens(String recognitionResult) {
        String[] tokens = recognitionResult.split("\\s+");
        List<String> normalizedTokens = new ArrayList<String>();
        for (String token: tokens) {
            String normalizedToken = normalizeSingleToken(token);
            if (normalizedToken.length() > 0) {
                normalizedTokens.add(normalizedToken);
            }
        }
        return normalizedTokens;
    }

    private static List<String> applyUnaryOperations(List<String> tokens) {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < tokens.size(); ++i) {
            String token = tokens.get(i);
            if (UNARY_OPERATIONS.containsKey(token)) {
                result.add(UNARY_OPERATIONS.get(token));
                result.add("(");
                result.add(tokens.get(++i));
                result.add(")");
            } else {
                result.add(token);
            }
        }
        return result;
    }

    private static List<String> applyBinaryOperations(List<String> tokens) {
        List<String> result = new ArrayList<String>();
        return tokens;
    }

    private static String joinTokens(List<String> tokens) {
        return Joiner.on(" ").join(tokens);
    }

    private String convertToMathExpression(String recognitionOutput) {
        return joinTokens(
                applyBinaryOperations(applyUnaryOperations(normalizeTokens(recognitionOutput)))
        );
    }

    private void evaluate(String mathExpression) {
        try {
            this.evaluatedValue = String.format(
                    "%.2f",
                    Double.parseDouble(mEvaluator.evaluate(mathExpression))
            );
            this.isEvaluated = true;
        } catch (EvaluationException exception) {
            this.evaluatedValue = "undefined";
            this.isEvaluated = false;
        }
    }

    public String getRecognitionOutput() {
        return recognitionOutput;
    }

    public String getNormalizedGuess() {
        return mathExpression;
    }

    public String getEvaluatedValue() {
        return evaluatedValue;
    }

    public boolean getIsEvaluated() {
        return isEvaluated;
    }

    @Override
    public String toString() {
        return this.getRecognitionOutput() + ": " + this.getNormalizedGuess() + ": "
                + this.getEvaluatedValue();
    }

    public String toTTSForm() {
        return this.getRecognitionOutput() + " is " + this.getEvaluatedValue();
    }
}