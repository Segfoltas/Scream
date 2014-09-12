package lt.segfoltas.scream;

import com.bugsense.trace.BugSenseHandler;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ToggleButton;

public class MainActivity extends Activity{
	
	private ImageView pebbleScreen;
	private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BugSenseHandler.initAndStartSession(this, "047aa837");
		setContentView(R.layout.activity_main);
		pebbleScreen = (ImageView) findViewById(R.id.pebble_screen);
		ToggleButton button = (ToggleButton) findViewById(R.id.service_switch);
		button.setOnCheckedChangeListener(listener);
		button.setChecked(isServiceRunning(ScreamService.class));
		registerReceiver(receiver, new IntentFilter(ScreamService.INTENT));
		startService(new Intent(this, ScreamService.class));
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, final Intent intent) {
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					if(intent.getAction().equals(ScreamService.INTENT)){
						if(intent.getBooleanExtra(ScreamService.VALUE, false)){
							pebbleScreen.setImageResource(R.drawable.screen_on);
						}else{
							pebbleScreen.setImageResource(R.drawable.screen_off);
						}
					}
				}
			});
			
		}
	};
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
		Log.d("MainActivity", "destroyed");
	}
	
	private OnCheckedChangeListener listener = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked)
				startService(new Intent(MainActivity.this, ScreamService.class));
			else
				stopService(new Intent(MainActivity.this, ScreamService.class));
		}
	};
	
	private boolean isServiceRunning(Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
}
