package lt.segfoltas.scream;

import java.util.UUID;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class PebbleInterface {

	private WearableCallbacks callbacks;
	private Handler handler;
	private long timeout;
	private PebbleDataReceiver receiver;
	private Context context;
	private UUID uuid;

	public PebbleInterface(final Context context, final WearableCallbacks callbacks, final UUID uuid, final long timeout) {
		this.callbacks = callbacks;
		this.context = context;
		this.handler = new Handler();
		this.timeout = timeout;
		this.uuid = uuid;
		connect();
	}
	
	public void connect(){

		PebbleKit.registerPebbleConnectedReceiver(context, connectedReceiver);

		PebbleKit.registerPebbleDisconnectedReceiver(context, disconnectedReceiver);

		receiver = new PebbleDataReceiver(uuid) {

			@Override
			public void receiveData(Context context, int transactionId, PebbleDictionary data) {
				final long shake = data.getUnsignedInteger(1);
				handler.post(new ShakeRunnable((int) shake));
				PebbleKit.sendAckToPebble(context, transactionId);
			}
		};

		PebbleKit.registerReceivedDataHandler(context, receiver);
	}
	
	public void disconnect(){
		context.unregisterReceiver(receiver);
		context.unregisterReceiver(connectedReceiver);
		context.unregisterReceiver(disconnectedReceiver);
		handler.removeCallbacks(timer);
	}

	private class ShakeRunnable implements Runnable {
		private final int shake;

		public ShakeRunnable(final int shake) {
			this.shake = shake;
		}

		@Override
		public void run() {
			callbacks.onShakeReceived(shake);
		}

	}

	private BroadcastReceiver connectedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateTimer();
			handler.post(new Runnable() {

				@Override
				public void run() {
					callbacks.onDeviceConnected();
				}
			});
		}
	};

	private BroadcastReceiver disconnectedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					callbacks.onDeviceDisconnected();
				}
			});
		}
	};

	private void updateTimer() {
		handler.removeCallbacks(timer);
		handler.postDelayed(timer, timeout);
	}

	private Runnable timer = new Runnable() {

		@Override
		public void run() {
			callbacks.onTimeout();
		}
	};
}
