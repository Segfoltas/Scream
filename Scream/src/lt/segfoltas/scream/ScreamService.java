package lt.segfoltas.scream;

import java.io.IOException;
import java.util.UUID;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ScreamService extends Service {
	
	public static final String INTENT = "lt.segfoltas.scream.indicatorintent";
	public static final String VALUE = "screaming";
	
	private static final UUID PEBBLE_APP_UUID = UUID.fromString("7f84367c-1f86-4491-a6bb-cdedbb55baa1");
	private static final int THRESHHOLD = 3000;
	
	private int silenceCount = 0;
	private MediaPlayer player;
	private int userVolume;
	private int maxVolume;
	private AudioManager manager;
	private PebbleInterface pebble;

	@Override
	public void onCreate() {
		super.onCreate();
		startForeground(1, getNotification());
		Log.d("ScreamService", "created");
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
		
		pebble = new PebbleInterface(getApplicationContext(), new Callbacks(){

			@Override
			public void onDeviceConnected() {
				//disconnect other devices
			}
			
		}, PEBBLE_APP_UUID, 2000);
	}
	
	private Notification getNotification(){
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(android.R.drawable.ic_delete);
		builder.setContentTitle("Scream");
		builder.setContentText("Ready to scream");
		builder.setOngoing(true);
		builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
		return builder.build();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("ScreamService", "destroyed");
		stop();
		pebble.disconnect();
		pebble = null;
	}
	
	private abstract class Callbacks implements WearableCallbacks{
		
		public void onDeviceDisconnected(){
			Log.d("ScreamService", "disconnected");
			stop();
		}
		
		public void onTimeout(){
			Log.d("ScreamService", "timeout");
			stop();
		}
		
		public void onShakeReceived(int shake){
			Log.d("ScreamService", "received");
			update(shake);
		}
	}
	
	private void update(int shake){
		Log.d("ScreamService", "updated");
		boolean scream;
		if(shake > THRESHHOLD){
			silenceCount = 0;
			scream = true;
			
		}else{
			silenceCount++;
			scream = silenceCount < 2;
			if(silenceCount > 2)
				silenceCount = 2;
		}
			
		if(scream)
			start();
		else if(player.isPlaying()){
			stop();
		}
	}
	
	private void start(){
		Log.d("ScreamService", "started");
		sendBroadcast(getScreamIntent(true));
		player.start();
		manager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_PLAY_SOUND);
	}
	
	private void stop(){
		Log.d("ScreamService", "stopped");
		sendBroadcast(getScreamIntent(false));
		player.stop();
		manager.setStreamVolume(AudioManager.STREAM_MUSIC, userVolume, AudioManager.FLAG_PLAY_SOUND);
		try {
			player.prepare();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Intent getScreamIntent(boolean screaming){
		Intent intent = new Intent(INTENT);
		intent.putExtra(VALUE, screaming);
		return intent;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
