package info.androidhive.speechtotext.speech_calculator;

import com.google.common.base.Joiner;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecognitionGuess {
    private static Set<String> IGNORED_WORDS = new HashSet<String>() {{
        add("how");
        add("much");
        add("is");
        add("you");
        add("by");
    }};

    private static Evaluator mEvaluator = new Evaluator();

    private String recognitionOutput;
    private String normalizedGuess;
    private String evaluatedValue;
    private boolean isEvaluated;

    RecognitionGuess(String recognitionOutput) {
        this.recognitionOutput = recognitionOutput;
        normalizeGuess();
        evaluate();
    }

    private static String normalizeSingleToken(String token) {
        if (token.equals("times")) {
            return "*";
        } else if (token.startsWith("divi")) {
            return "/";
        } else if (token.startsWith("free")) {
            return "3";
        } else if (IGNORED_WORDS.contains(token)) {
            return "";
        }
        return token;
    }

    private static String normalizeTokens(String recognitionResult) {
        String[] tokens = recognitionResult.split("\\s+");
        List<String> normalizedTokens = new ArrayList<String>();
        for (String token: tokens) {
            normalizedTokens.add(normalizeSingleToken(token));
        }
        return Joiner.on(" ").join(normalizedTokens);
    }

    private void normalizeGuess() {
        this.normalizedGuess = normalizeTokens(this.recognitionOutput);
    }

    private void evaluate() {
        try {
            this.evaluatedValue = String.format(
                    "%.2f",
                    Double.parseDouble(mEvaluator.evaluate(this.getNormalizedGuess()))
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
        return normalizedGuess;
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
        return this.getNormalizedGuess() + " is " + this.getEvaluatedValue();
    }
}