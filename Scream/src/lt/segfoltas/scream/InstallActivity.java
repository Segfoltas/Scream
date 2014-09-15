package lt.segfoltas.scream;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class InstallActivity extends Activity {
	
	private Intent pebbleIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings);
		pebbleIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("pebble://appstore/5405e9c42c4f7fb698000022"));
		pebbleIntent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK );
		
		View pebbleButton = findViewById(R.id.install_pebble);
		pebbleButton.setOnClickListener(onPebbleClick);
		pebbleButton.setEnabled(hasPebble());
	}
	
	private View.OnClickListener onPebbleClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			startActivity(pebbleIntent);
		}
	};

	private boolean hasPebble(){
		PackageManager packageManager = getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(pebbleIntent, 0);
		
		return activities.size() != 0;
	}

}
