package io.mainframe.hacs.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.mainframe.hacs.common.Constants;

public class NetworkStatus extends BroadcastReceiver implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = NetworkStatus.class.getName();
    public static final String PREFS_REQUIRE_MAINFRAME_WIFI = "requireMainframeWifi";

    private final Context context;

    private boolean requireMainframeWifi;
    private final List<NetworkStatusListener> allListener =
            Collections.synchronizedList(new ArrayList<NetworkStatusListener>());
    private boolean hasWifi, hasMobile, isInMainframeWifi;

    public NetworkStatus(Context ctx, SharedPreferences prefs) {
        context = ctx;
        requireMainframeWifi = prefs.getBoolean(PREFS_REQUIRE_MAINFRAME_WIFI, true);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    public boolean hasNetwork() {
        return hasWifi || hasMobile;
    }

    public boolean hasWifi() {
        return hasWifi;
    }

    public boolean hasMobile() {
        return hasMobile;
    }

    public boolean isInMainframeWifi() {
        return isInMainframeWifi;
    }

    public void addListener(NetworkStatusListener listener) {
        allListener.add(listener);
    }

    public void removeListener(NetworkStatusListener listener) {
        final Iterator<NetworkStatusListener> iter = allListener.iterator();
        while (iter.hasNext()) {
            if (iter.next() == listener) {
                iter.remove();
                return;
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        parseResult(context);
        Log.d(TAG, String.format("onReceive %s: hasNetwork=%s, hasMobile=%s, hasWifi=%s, isInMainframeWifi=%s",
                intent.getAction(), hasNetwork(), hasMobile, hasWifi, isInMainframeWifi));
        updateListener();
    }

    private void updateListener() {
        for (NetworkStatusListener listener : allListener) {
            listener.onNetworkChange(hasNetwork(), hasMobile, hasWifi, isInMainframeWifi);
        }
    }

    private void parseResult(Context context) {
        hasWifi = false;
        hasMobile = false;
        isInMainframeWifi = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Network[] allNetworks = cm.getAllNetworks();
        for (Network network : allNetworks) {
            final NetworkInfo ni = cm.getNetworkInfo(network);

            if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
                if (ni.isConnected()) {
                    hasWifi = true;
                }
            } else if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {
                if (ni.isConnected()) {
                    hasMobile = true;
                }
            }
        }

        if (!hasWifi) {
            Log.d(TAG, "No wifi connection.");
            return;
        }

        if (!requireMainframeWifi) {
            this.isInMainframeWifi = true;
            Log.i(TAG, "requireMainframeWifi = false");
            return;
        }


        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

        Log.d(TAG, String.format("Wifi found ssid: %s, bssid: %s", wifiInfo.getSSID(), wifiInfo.getBSSID()));
        for (String ssid : Constants.MAINFRAME_SSIDS) {
            String quotedSsid = "\"" + ssid + "\"";
            if (quotedSsid.equals(wifiInfo.getSSID())) {
                isInMainframeWifi = true;
                Log.i(TAG, "Mainframe wifi found.");
                return;
            }
        }

        Log.d(TAG, "Wrong wifi, you must connect to a mainframe wifi.");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!PREFS_REQUIRE_MAINFRAME_WIFI.equals(key)) {
            return;
        }

        requireMainframeWifi = sharedPreferences.getBoolean(PREFS_REQUIRE_MAINFRAME_WIFI, true);
        parseResult(context);
        updateListener();
    }

    public interface NetworkStatusListener {
        void onNetworkChange(boolean hasNetwork, boolean hasMobile, boolean hasWifi, boolean isInMainframeWifi);
    }
}
