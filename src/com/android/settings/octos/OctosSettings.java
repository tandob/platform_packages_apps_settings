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
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;

import com.android.internal.logging.MetricsLogger;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.util.List;

public class OctosSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_HIDE_TENTACLE_ICON = "hide_tentacles_icon";
    private static final String SUPERSU_APP_PACKAGE = "eu.chainfire.supersu";
    private static final String KERNELADIUTOR_APP_PACKAGE = "com.grarak.kerneladiutor";

    private SwitchPreference mHideTentaclesIcon;

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

        if (!Utils.isPackageInstalled(getActivity(), SUPERSU_APP_PACKAGE)) {
            getPreferenceScreen().removePreference(findPreference("supersu_settings"));
        }

        if (!Utils.isPackageInstalled(getActivity(), KERNELADIUTOR_APP_PACKAGE)) {
            getPreferenceScreen().removePreference(findPreference("kerneladiutor_settings"));
        }

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // If we didn't handle it, let preferences handle it.
        return super.onPreferenceTreeClick(preferenceScreen, preference);
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
        return true;
    }

}
