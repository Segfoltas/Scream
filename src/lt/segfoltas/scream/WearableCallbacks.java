package lt.segfoltas.scream;

public interface WearableCallbacks {

	public void onDeviceConnected();
	public void onDeviceDisconnected();
	public void onTimeout();
	public void onShakeReceived(int shake);
}
