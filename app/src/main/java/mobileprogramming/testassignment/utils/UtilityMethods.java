package mobileprogramming.testassignment.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import static mobileprogramming.testassignment.utils.Constant.REQ_PERMISSION;

public class UtilityMethods {

    /**
     * Used to check Location Permission > 5.0
     *
     * @return true if granted else false
     */
    public static boolean checkLocationPermission(@NonNull Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Used to set network info in application global class
     */
    public static void setNetworkPrefrence(@NonNull Activity context) {
        NetworkInfo activeNetworkInfo = getNetworkInfo(context);
        if (activeNetworkInfo != null) {
            TestApplication testApplication = (TestApplication) context.getApplication();
            testApplication.setNetworkName(activeNetworkInfo.getExtraInfo());
            testApplication.setNetworkType(activeNetworkInfo.getTypeName());
        }
    }

    /**
     * Used to get current network info
     *
     * @return NetworkInfo Object
     */
    public static NetworkInfo getNetworkInfo(@NonNull Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    /**
     * used to request permission
     *
     * @param permission permission array
     */
    public static void requestPermissions(@NonNull Activity context, String[] permission) {
        ActivityCompat.requestPermissions(context,
                permission,
                REQ_PERMISSION);
    }

    /**
     * App cannot work without the permissions
     */
    public static void permissionsDenied(@NonNull Activity context) {
        Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show();
        context.finish();
    }
}
