package io.mainframe.hacs.ssh;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.regex.Pattern;

import io.mainframe.hacs.R;

/**
 * Created by holger on 29.11.15.
 */
public class PkCredentials {

    public final String privateKeyFile;
    public final String password;

    public PkCredentials(SharedPreferences sharedPreferences, Context context) {
        this.privateKeyFile = sharedPreferences.getString(
                context.getString(R.string.PREFS_PRIVATE_KEY_FILENAME), null);
        this.password = sharedPreferences.getString(
                context.getString(R.string.PREFS_PRIVATE_KEY_PASSWORD), null);
    }

    /**
     * Gets the "lalafoo" from "mf-door.lalafoo.key"
     *
     * @return the user, based on the name of the key file
     */
    public String getUser() {
        File pkFile = new File(this.privateKeyFile);
        String[] splits = pkFile.getName().split(Pattern.quote("."));
        if (splits.length != 3) {
            throw new IllegalArgumentException("Invalid private key filename: " + pkFile.getName());
        }
        return splits[1];
    }

    public boolean isPasswordSet() {
        return password != null && !password.isEmpty();
    }
}
