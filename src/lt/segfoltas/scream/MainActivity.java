package lt.segfoltas.scream;

import java.io.IOException;
import java.util.UUID;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.R.color;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final UUID PEBBLE_APP_UUID = UUID.fromString("7f84367c-1f86-4491-a6bb-cdedbb55baa1");
	private static final int THRESHHOLD = 5000;
	
	private Handler handler = new Handler();
	private TextView info;
	private boolean showing = false;
	private int silenceCount = 0;
	private MediaPlayer player;
	private int userVolume;
	private int maxVolume;
	private AudioManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		info = (TextView) findViewById(R.id.shakiness);
		player = new MediaPlayer();
		AssetFileDescriptor afd;
		try {
			afd = getAssets().openFd("AttackKamikaze.wav");
			player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			player.setLooping(true);
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		    player.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}
		manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		userVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
		maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		PebbleKit.registerReceivedDataHandler(this, receiver);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		showing = true;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		showing = false;
	}	
	
	private PebbleDataReceiver receiver = new PebbleDataReceiver(PEBBLE_APP_UUID) {
		
		@Override
		public void receiveData(Context context, int transactionId,
				PebbleDictionary data) {
			if(showing){
				final long shake = data.getUnsignedInteger(1);
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						update((int) shake);
					}
				});
			}
			PebbleKit.sendAckToPebble(context, transactionId);
		}
	};
	
	private void update(int shake){
		boolean scream;
		if(shake > THRESHHOLD){
			silenceCount = 0;
			scream = true;
			
		}else{
			silenceCount ++;
			scream = silenceCount < 2;
			if(silenceCount > 2)
				silenceCount = 2;
		}
			
		info.setText("Shake: " + String.valueOf(shake));
		info.setTextColor(getResources().getColor(scream ? android.R.color.holo_red_dark : android.R.color.black));
		if(scream)
			start();
		else if(player.isPlaying()){
			stop();
		}
	}
	
	private void start(){
		player.start();
		manager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_PLAY_SOUND);
	}
	
	private void stop(){
		player.stop();
		manager.setStreamVolume(AudioManager.STREAM_MUSIC, userVolume, AudioManager.FLAG_PLAY_SOUND);
		try {
			player.prepare();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
