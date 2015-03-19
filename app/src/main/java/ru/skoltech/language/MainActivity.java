package ru.skoltech.language;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Locale;

import ru.skoltech.language.speech_calculator.RecognitionGuess;
import ru.skoltech.language.speech_calculator.SpeechCalculator;

public class MainActivity extends Activity {

	private TextView txtQuery, txtEvaluatedResult;
	private ImageButton btnSpeak;
	private final int REQ_CODE_SPEECH_INPUT = 100;
    private TextToSpeech ttobj;
    public static String language = "en_US";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR){
                    if (language.equals("en_US"))
                        ttobj.setLanguage(Locale.US);
                    else {
                        Locale locale = new Locale("ru");
                        ttobj.setLanguage(locale);
                    }
                }
            }
        });

        txtQuery = (TextView) findViewById(R.id.txtQuery);
        txtEvaluatedResult = (TextView) findViewById(R.id.txtEvaluatedResult);
		btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

		btnSpeak.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				promptSpeechInput();
			}
		});

	}

	/**
	 * Showing google speech input dialog
	 * */
	private void promptSpeechInput() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				getString(R.string.speech_prompt));
		try {
			startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
		} catch (ActivityNotFoundException a) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.speech_not_supported),
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Receiving speech input
	 * */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case REQ_CODE_SPEECH_INPUT: {
			if (resultCode == RESULT_OK && null != data) {
				ArrayList<String> recognitionResults = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                RecognitionGuess bestResult =
                        SpeechCalculator.processRecognitionResult(recognitionResults).get(0);

                Log.d("BEST_RESULT", bestResult.toString());

                txtQuery.setText(bestResult.getNormalizedGuess());
                txtEvaluatedResult.setText(bestResult.getEvaluatedValue());
                ttobj.speak(bestResult.toTTSForm(), TextToSpeech.QUEUE_FLUSH, null);
            }
			break;
		}

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    public void onToggleClicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        if (on) {
            language = "ru_RU";
            ttobj.setLanguage(new Locale("ru"));
        } else {
            language = "en_US";
            ttobj.setLanguage(Locale.US);
        }
    }

}
