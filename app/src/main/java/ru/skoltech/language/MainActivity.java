package ru.skoltech.language;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kevinsawicki.http.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends Activity {

	private TextView txtQuery;
	private ImageButton btnSpeak;
	private final int REQ_CODE_SPEECH_INPUT = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // http://stackoverflow.com/questions/22395417/error-strictmodeandroidblockguardpolicy-onnetwork
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        txtQuery = (TextView) findViewById(R.id.txtQuery);
		btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

		btnSpeak.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});

        new RecognizeChunkTask().execute();
	}

    class RecognizeChunkTask extends AsyncTask<Void, Void, Void> {
        private String transcript;

        @Override
        protected Void doInBackground(Void... params) {
            byte[] PCMSound = writeSpeechChunkToPCM();

            new RecognizeChunkTask().execute();

            String text = recognizePCMFile(PCMSound);
            Log.i("JSON RESPONSE", text);
            
            try {
                transcript = new JSONObject(text.split("\\r?\\n")[1]).getJSONArray("result")
                        .getJSONObject(0).getJSONArray("alternative").getJSONObject(0)
                        .getString("transcript");
                Log.i("TRANSCRIPT", transcript);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (transcript != null) {
                txtQuery.setText(txtQuery.getText() + "\n" + transcript);
            }
        }

        private String recognizePCMFile(byte[] PCMSound) {
            HttpRequest request = HttpRequest.post("https://www.google.com/speech-api/v2/recognize",
                    true, "output", "json", "lang", "ru-ru", "key", "AIzaSyDbzyMzbi_vNpEvjhO_ob8mEVMxi70FUuI")
                    .header("Content-Type", "audio/l16; rate=16000;")
                    .trustAllCerts()
                    .trustAllHosts()
                    .send(PCMSound);

            return request.body();
        }

        private byte[] writeSpeechChunkToPCM() {
            int RECORDER_SAMPLERATE = 16000;
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
                    tempIndex++;
                    continue;
                }

                if( temp > 350 && recording == false )
                {
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

                    String fileName = file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".pcm";
                    Log.i("FILENAME", fileName);

                    int realSize = totalByteBuffer.length;
                    while (totalByteBuffer[realSize - 1] == 0) {
                        realSize--;
                    }

                    audioRecorder.stop();
                    return Arrays.copyOfRange(totalByteBuffer, 0, realSize);
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
