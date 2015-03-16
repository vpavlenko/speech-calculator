package info.androidhive.speechtotext.speech_calculator;

import com.google.common.base.Joiner;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vpavlenko on 16/03/15.
 */
public class SpeechCalculator {
    public static int countNumbersAndArithmeticOperations(String s) {
        Pattern pattern = Pattern.compile("\\d+|[\\.+*/%-]");
        Matcher matcher = pattern.matcher(s);

        int count = 0;
        while (matcher.find())
            count++;

        return count;
    }

    public static List<RecognitionGuess> processRecognitionResult(List<String> recognitionResults) {
        List<RecognitionGuess> normalizedResults = new ArrayList<RecognitionGuess>();
        for (String result: recognitionResults) {
            normalizedResults.add(new RecognitionGuess(result));
        }

        Collections.sort(normalizedResults, new Comparator<RecognitionGuess>() {
            @Override
            public int compare(RecognitionGuess g, RecognitionGuess g2) {
                return countNumbersAndArithmeticOperations(g2.getNormalizedGuess())
                       - countNumbersAndArithmeticOperations(g.getNormalizedGuess());
            }
        });

        Collections.sort(normalizedResults, new Comparator<RecognitionGuess>() {
            @Override
            public int compare(RecognitionGuess g, RecognitionGuess g2) {
                return -Boolean.compare(g.getIsEvaluated(), g2.getIsEvaluated());
            }
        });

        return normalizedResults;
    }

}
