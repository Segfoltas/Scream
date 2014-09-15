package lt.segfoltas.scream;

import java.io.IOException;
import java.util.UUID;

import lt.segfoltas.wearableinterface.PebbleProcessor;
import lt.segfoltas.wearableinterface.WearableCallbacks.ConnectedListener;
import lt.segfoltas.wearableinterface.WearableCallbacks.DataReceivedListener;
import lt.segfoltas.wearableinterface.WearableCallbacks.DisconnectedListener;
import lt.segfoltas.wearableinterface.WearableCallbacks.TimeoutListener;
import lt.segfoltas.wearableinterface.WearableIds;
import lt.segfoltas.wearableinterface.WearableInterface;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.getpebble.android.kit.util.PebbleDictionary;

public class ScreamService extends Service implements DisconnectedListener, TimeoutListener, DataReceivedListener<Integer>, ConnectedListener{
	
	public static final String INTENT = "lt.segfoltas.scream.indicatorintent";
	public static final String VALUE = "screaming";
	
	private static final int THRESHHOLD = 3000;
	private static final WearableIds IDS = new WearableIds(UUID.fromString("7f84367c-1f86-4491-a6bb-cdedbb55baa1"));
	
	private int silenceCount = 2;
	private MediaPlayer player;
	private int userVolume;
	private int maxVolume;
	private AudioManager manager;
	private WearableInterface<Integer> wearable;
	private Handler handler = new Handler();

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
		
		wearable = new WearableInterface<Integer>(getApplicationContext(), IDS);
		wearable.setTimeoutListener(this, 2000);
		wearable.setDisconnectedListener(this);
		wearable.setReceivedListener(this);
		wearable.setConnectedListener(this);
		wearable.setProcessor(new PebbleProcessor<Integer>() {
			
			@Override
			public Integer decode(PebbleDictionary data) {
				return data.getUnsignedInteger(1).intValue();
			}
		});
		wearable.connect();
		
		handler.removeCallbacks(connectionTimer);
		handler.postDelayed(connectionTimer, 3000);
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
		wearable.disconnect();
		wearable = null;
	}

	@Override
	public void onRecieve(Integer data) {
		Log.d("ScreamService", "received " + data.toString());
		update(data);
	}

	@Override
	public void onTimeout() {
		Log.d("ScreamService", "timeout");
		stop();
	}

	@Override
	public void onDisconnected() {
		Log.d("ScreamService", "disconnected");
		stop();
	}
	
	@Override
	public void onConnected() {
		Log.d("ScreamService", "connected");
		handler.removeCallbacks(connectionTimer);
	}
	
	private Runnable connectionTimer = new Runnable() {
		
		@Override
		public void run() {
			showNoPebbleNotif();
		}
	};
	
	private void showNoPebbleNotif(){
		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
		
		Intent intent = new Intent(this, InstallActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
		
		builder.setContentTitle("No Device");
		builder.setContentText("No wearable device found, click for solutions");
		builder.setSmallIcon(android.R.drawable.ic_delete);
		builder.setContentIntent(pi);
		
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(0, builder.build());
	}
	
	private void update(int shake){
		Log.d("ScreamService", "updated");
		boolean scream = false;
		
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
