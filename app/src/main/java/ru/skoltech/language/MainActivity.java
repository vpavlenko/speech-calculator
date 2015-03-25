package ru.skoltech.language;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends Activity {

	private TextView txtQuery;
	private ImageButton btnSpeak;
	private final int REQ_CODE_SPEECH_INPUT = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        txtQuery = (TextView) findViewById(R.id.txtQuery);
		btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

		btnSpeak.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				promptSpeechInputContinuous();
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
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en_US");
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

    private void promptSpeechInputContinuous() {
        int RECORDER_SAMPLERATE = 44100;
        byte RECORDER_BPP = 16;
        int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
        int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

        int bufferSizeInBytes = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING
        );

        // Initialize Audio Recorder.
        AudioRecord audioRecorder = new AudioRecord( MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                bufferSizeInBytes
        );
        // Start Recording.
        audioRecorder.startRecording();

        int numberOfReadBytes   = 0;
        byte audioBuffer[]      = new  byte[bufferSizeInBytes];
        boolean recording       = false;
        float tempFloatBuffer[] = new float[3];
        int tempIndex           = 0;
        int totalReadBytes      = 0;
        byte totalByteBuffer[]  = new byte[60 * RECORDER_SAMPLERATE * 1];


        // While data come from microphone.
        while( true )
        {
            float totalAbsValue = 0.0f;
            short sample        = 0;

            numberOfReadBytes = audioRecorder.read( audioBuffer, 0, bufferSizeInBytes );

            // Analyze Sound.
            for( int i=0; i<bufferSizeInBytes; i+=2 )
            {
                sample = (short)( (audioBuffer[i]) | audioBuffer[i + 1] << 8 );
                totalAbsValue += Math.abs( sample ) / (numberOfReadBytes/2);
            }

            // Analyze temp buffer.
            tempFloatBuffer[tempIndex%3] = totalAbsValue;
            float temp                   = 0.0f;
            for( int i=0; i<3; ++i )
                temp += tempFloatBuffer[i];

            if( (temp >=0 && temp <= 350) && recording == false )
            {
                Log.i("TAG", "1");
                tempIndex++;
                continue;
            }

            if( temp > 350 && recording == false )
            {
                Log.i("TAG", "2");
                recording = true;
            }

            if( (temp >= 0 && temp <= 350) && recording == true )
            {
                Log.i("TAG", "Save audio to file.");

                // Save audio to file.
                String filepath = Environment.getExternalStorageDirectory().getPath();
                File file = new File(filepath,"AudioRecorder");
                if( !file.exists() )
                    file.mkdirs();

                String fn = file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".wav";
                Log.i("FILENAME", fn);

                long totalAudioLen  = 0;
                long totalDataLen   = totalAudioLen + 36;
                long longSampleRate = RECORDER_SAMPLERATE;
                int channels        = 1;
                long byteRate       = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;
                totalAudioLen       = totalReadBytes;
                totalDataLen        = totalAudioLen + 36;
                byte finalBuffer[]  = new byte[totalReadBytes + 44];

                finalBuffer[0] = 'R';  // RIFF/WAVE header
                finalBuffer[1] = 'I';
                finalBuffer[2] = 'F';
                finalBuffer[3] = 'F';
                finalBuffer[4] = (byte) (totalDataLen & 0xff);
                finalBuffer[5] = (byte) ((totalDataLen >> 8) & 0xff);
                finalBuffer[6] = (byte) ((totalDataLen >> 16) & 0xff);
                finalBuffer[7] = (byte) ((totalDataLen >> 24) & 0xff);
                finalBuffer[8] = 'W';
                finalBuffer[9] = 'A';
                finalBuffer[10] = 'V';
                finalBuffer[11] = 'E';
                finalBuffer[12] = 'f';  // 'fmt ' chunk
                finalBuffer[13] = 'm';
                finalBuffer[14] = 't';
                finalBuffer[15] = ' ';
                finalBuffer[16] = 16;  // 4 bytes: size of 'fmt ' chunk
                finalBuffer[17] = 0;
                finalBuffer[18] = 0;
                finalBuffer[19] = 0;
                finalBuffer[20] = 1;  // format = 1
                finalBuffer[21] = 0;
                finalBuffer[22] = (byte) channels;
                finalBuffer[23] = 0;
                finalBuffer[24] = (byte) (longSampleRate & 0xff);
                finalBuffer[25] = (byte) ((longSampleRate >> 8) & 0xff);
                finalBuffer[26] = (byte) ((longSampleRate >> 16) & 0xff);
                finalBuffer[27] = (byte) ((longSampleRate >> 24) & 0xff);
                finalBuffer[28] = (byte) (byteRate & 0xff);
                finalBuffer[29] = (byte) ((byteRate >> 8) & 0xff);
                finalBuffer[30] = (byte) ((byteRate >> 16) & 0xff);
                finalBuffer[31] = (byte) ((byteRate >> 24) & 0xff);
                finalBuffer[32] = (byte) (2 * 16 / 8);  // block align
                finalBuffer[33] = 0;
                finalBuffer[34] = RECORDER_BPP;  // bits per sample
                finalBuffer[35] = 0;
                finalBuffer[36] = 'd';
                finalBuffer[37] = 'a';
                finalBuffer[38] = 't';
                finalBuffer[39] = 'a';
                finalBuffer[40] = (byte) (totalAudioLen & 0xff);
                finalBuffer[41] = (byte) ((totalAudioLen >> 8) & 0xff);
                finalBuffer[42] = (byte) ((totalAudioLen >> 16) & 0xff);
                finalBuffer[43] = (byte) ((totalAudioLen >> 24) & 0xff);

                for( int i=0; i<totalReadBytes; ++i )
                    finalBuffer[44+i] = totalByteBuffer[i];

                FileOutputStream out;
                try {
                    out = new FileOutputStream(fn);
                    out.write(finalBuffer);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                tempIndex++;
                break;
            }

            // -> Recording sound here.
            Log.i( "TAG", "Recording Sound." );
            for( int i=0; i<numberOfReadBytes; i++ )
                totalByteBuffer[totalReadBytes + i] = audioBuffer[i];
            totalReadBytes += numberOfReadBytes;
            //*/

            tempIndex++;
        }

    }

    private void promptSpeechInputNew() {
//        SpeechRecognizer sr = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
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
                txtQuery.setText(recognitionResults.get(0));
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

}
