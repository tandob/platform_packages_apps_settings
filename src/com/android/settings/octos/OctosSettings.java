/*
 * Copyright (C) 2015 Team OctOs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.octos;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;
import android.util.DisplayMetrics;
import android.util.Log;

import com.android.internal.logging.MetricsLogger;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.List;

public class OctosSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "OctosSettings";
    private static final String KEY_HIDE_TENTACLE_ICON = "hide_tentacles_icon";
    private static final String KEY_LCD_DENSITY = "lcd_density";

    private SwitchPreference mHideTentaclesIcon;
    private ListPreference mLcdDensityPreference;

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.DEVELOPMENT;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.octos_settings);

        mHideTentaclesIcon = (SwitchPreference) findPreference(KEY_HIDE_TENTACLE_ICON);
        mHideTentaclesIcon.setOnPreferenceChangeListener(this);

        mLcdDensityPreference = (ListPreference) findPreference(KEY_LCD_DENSITY);
        if (mLcdDensityPreference != null) {
            final String defaultText = getResources().getString(R.string.lcd_density_default);
            int defaultDensity = DisplayMetrics.DENSITY_DEVICE;
            int currentDensity = DisplayMetrics.DENSITY_CURRENT;
            if (currentDensity < 10 || currentDensity >= 1000) {
                // Unsupported value, force default
                currentDensity = defaultDensity;
            }
            int factor = 40;
            if (defaultDensity > 400) {
                factor = 80;
            }
            int minimumDensity = (defaultDensity - (factor * 2));
            int currentIndex = -1;
            String[] densityEntries = new String[5];
            String[] densityValues = new String[5];
            for (int idx = 0; idx < 5; ++idx) {
                int val = (minimumDensity + (factor * idx));
                densityEntries[idx] = Integer.toString(val);
                if (val == defaultDensity) {
                    densityEntries[idx] += " (" + defaultText + ")";
                }
                densityValues[idx] = Integer.toString(val);
                if (currentDensity == val) {
                    currentIndex = idx;
                }
            }
            mLcdDensityPreference.setEntries(densityEntries);
            mLcdDensityPreference.setEntryValues(densityValues);
            if (currentIndex != -1) {
                mLcdDensityPreference.setValueIndex(currentIndex);
            }
            mLcdDensityPreference.setOnPreferenceChangeListener(this);
            updateLcdDensityPreferenceDescription(currentDensity);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // If we didn't handle it, let preferences handle it.
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void updateLcdDensityPreferenceDescription(int currentDensity) {
        final String defaultText = getResources().getString(R.string.lcd_density_default);
        int defaultDensity = DisplayMetrics.DENSITY_DEVICE;
        ListPreference preference = mLcdDensityPreference;
        String summary = getResources().getString(R.string.lcd_density_summary, currentDensity);
        if (currentDensity == defaultDensity) {
            summary += " (" + defaultText + ")";
        }
        preference.setSummary(summary);
    }

    private void writeLcdDensityPreference(final Context context, int value) {
        try {
            SystemProperties.set("persist.sys.lcd_density", Integer.toString(value));
        } catch (RuntimeException e) {
            Log.e(TAG, "Unable to save LCD density");
            return;
        }
        final IActivityManager am = ActivityManagerNative.asInterface(
                ServiceManager.checkService("activity"));
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                ProgressDialog dialog = new ProgressDialog(context);
                dialog.setMessage(getResources().getString(R.string.restarting_ui));
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.show();
            }
            @Override
            protected Void doInBackground(Void... params) {
                // Give the user a second to see the dialog
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // Ignore
                }
                // Restart the UI
                try {
                    am.restart();
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to restart");
                }
                return null;
            }
        };
        task.execute();
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (KEY_HIDE_TENTACLE_ICON.equals(key)) {
            if ((Boolean) objValue) {
            PackageManager p=this.getPackageManager();
            p.setComponentEnabledSetting(new ComponentName("com.android.settings","com.android.settings.Settings$OctosSettingsActivity"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);

            } else {
            PackageManager p = getPackageManager();
            ComponentName componentName = new ComponentName("com.android.settings","com.android.settings.Settings$OctosSettingsActivity");
            p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            }
        }
        if (KEY_LCD_DENSITY.equals(key)) {
            try {
                int value = Integer.parseInt((String) objValue);
                writeLcdDensityPreference(preference.getContext(), value);
                updateLcdDensityPreferenceDescription(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist display density setting", e);
            }
        }
        return true;
    }

}
