package mobileprogramming.testassignment;

import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LandingActivity extends Activity implements
        View.OnClickListener {

    private static final String TAG = LandingActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        //check for GPS settings
        checkForGPS();

        //initialize views
        initView();
    }

    /**
     * Used to check GPS settings
     */
    private void checkForGPS() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check if enabled and if not send user to the GSP settings
        if (!enabled) {
            Toast.makeText(this, R.string.location_services_info, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    /**
     * Init views associated with activity
     */
    private void initView() {
        TextView tvLat = findViewById(R.id.lat);
        TextView tvLong = findViewById(R.id.lon);
        Button btnCreateGeofence = findViewById(R.id.btn_create_geofence);
        btnCreateGeofence.setOnClickListener(this);
        Button btnRemoveGeofence = findViewById(R.id.btn_clear_geofence);
        btnRemoveGeofence.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_create_geofence:
                createGeofence();
                break;
            case R.id.btn_clear_geofence:
                clearGeofence();
                break;
        }
    }

    /**
     * Used to clear created GeoFence
     */
    private void clearGeofence() {

    }

    /**
     * Used to create GeoFence
     */
    private void createGeofence() {

    }
}
