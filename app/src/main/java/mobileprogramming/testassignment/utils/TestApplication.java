package mobileprogramming.testassignment.utils;

import android.app.Application;

public class TestApplication extends Application {
    private String networkName;
    private String networkType;

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }
}
