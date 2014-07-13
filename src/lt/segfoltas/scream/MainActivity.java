package lt.segfoltas.scream;

import java.util.UUID;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final UUID PEBBLE_APP_UUID = UUID.fromString("7f84367c-1f86-4491-a6bb-cdedbb55baa1");
	
	private Handler handler = new Handler();
	private TextView info;
	private boolean showing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		info = (TextView) findViewById(R.id.shakiness);
		PebbleKit.registerReceivedDataHandler(this, receiver);
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
		info.setText("Shake: " + String.valueOf(shake));
	}
}
