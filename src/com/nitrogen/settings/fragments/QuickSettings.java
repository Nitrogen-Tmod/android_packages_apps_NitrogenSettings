package com.nitrogen.settings.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import androidx.preference.ListPreference;
import com.nitrogen.settings.preferences.CustomSeekBarPreference;
import com.nitrogen.settings.preferences.SystemSettingSwitchPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import java.util.Locale;
import android.text.TextUtils;
import android.view.View;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class QuickSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {
        private Preference mHeaderBrowse;
    private ListPreference mDaylightHeaderPack;
    private CustomSeekBarPreference mHeaderShadow;
    private ListPreference mHeaderProvider;
    private String mDaylightHeaderProvider;
    private SwitchPreference mHeaderEnabled;
    private CustomSeekBarPreference mQsPanelAlpha;

    private static final String CUSTOM_HEADER_BROWSE = "custom_header_browse";
    private static final String CUSTOM_HEADER_IMAGE = "status_bar_custom_header";
    private static final String DAYLIGHT_HEADER_PACK = "daylight_header_pack";
    private static final String CUSTOM_HEADER_IMAGE_SHADOW = "status_bar_custom_header_shadow";
    private static final String CUSTOM_HEADER_PROVIDER = "custom_header_provider";
    private static final String STATUS_BAR_CUSTOM_HEADER = "status_bar_custom_header";
    private static final String CUSTOM_HEADER_ENABLED = "status_bar_custom_header";
    private static final String KEY_QS_PANEL_ALPHA = "qs_panel_alpha";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.nitrogen_settings_quicksettings);

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        mHeaderBrowse = findPreference(CUSTOM_HEADER_BROWSE);
        mHeaderBrowse.setEnabled(isBrowseHeaderAvailable());

        mHeaderEnabled = (SwitchPreference) findPreference(CUSTOM_HEADER_ENABLED);
        mHeaderEnabled.setOnPreferenceChangeListener(this);

        mDaylightHeaderPack = (ListPreference) findPreference(DAYLIGHT_HEADER_PACK);

        List<String> entries = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        getAvailableHeaderPacks(entries, values);
        mDaylightHeaderPack.setEntries(entries.toArray(new String[entries.size()]));
        mDaylightHeaderPack.setEntryValues(values.toArray(new String[values.size()]));

        boolean headerEnabled = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CUSTOM_HEADER, 0) != 0;
        updateHeaderProviderSummary(headerEnabled);
        mDaylightHeaderPack.setOnPreferenceChangeListener(this);
        mHeaderShadow = (CustomSeekBarPreference) findPreference(CUSTOM_HEADER_IMAGE_SHADOW);
        final int headerShadow = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, 0);
        mHeaderShadow.setValue((int)(((double) headerShadow / 255) * 100));
        mHeaderShadow.setOnPreferenceChangeListener(this);

        mDaylightHeaderProvider = getResources().getString(R.string.daylight_header_provider);
        String providerName = Settings.System.getString(resolver,
                Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER);
        if (providerName == null) {
            providerName = mDaylightHeaderProvider;
        }
        mHeaderProvider = (ListPreference) findPreference(CUSTOM_HEADER_PROVIDER);
        int valueIndex = mHeaderProvider.findIndexOfValue(providerName);
        mHeaderProvider.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mHeaderProvider.setSummary(mHeaderProvider.getEntry());
        mHeaderProvider.setOnPreferenceChangeListener(this);
        mDaylightHeaderPack.setEnabled(providerName.equals(mDaylightHeaderProvider));
    }

    private void updateHeaderProviderSummary(boolean headerEnabled) {
        mDaylightHeaderPack.setSummary(getResources().getString(R.string.header_provider_disabled));
        if (headerEnabled) {
            String settingHeaderPackage = Settings.System.getString(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK);
            int valueIndex = mDaylightHeaderPack.findIndexOfValue(settingHeaderPackage);
            if (valueIndex == -1) {
                // no longer found
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUS_BAR_CUSTOM_HEADER, 0);
            } else {
                mDaylightHeaderPack.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
                mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntry());
            }
        }
        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

      mQsPanelAlpha = (CustomSeekBarPreference) findPreference(KEY_QS_PANEL_ALPHA);
        int qsPanelAlpha = Settings.System.getInt(getContentResolver(),
                Settings.System.QS_PANEL_BG_ALPHA, 221);
        mQsPanelAlpha.setValue((int)(((double) qsPanelAlpha / 255) * 100));
        mQsPanelAlpha.setOnPreferenceChangeListener(this);
     }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
       ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mQsPanelAlpha) {
            int bgAlpha = (Integer) newValue;
            int trueValue = (int) (((double) bgAlpha / 100) * 255);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.QS_PANEL_BG_ALPHA, trueValue);
            return true;
        }

        switch (preference.getKey()) {
        case DAYLIGHT_HEADER_PACK:
                String dhvalue = (String) newValue;
                Settings.System.putString(resolver,
                        Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK, dhvalue);
                int dhvalueIndex = mDaylightHeaderPack.findIndexOfValue(dhvalue);
                mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntries()[dhvalueIndex]);
                return true;

            case CUSTOM_HEADER_IMAGE_SHADOW:
                Integer headerShadow = (Integer) newValue;
                int realHeaderValue = (int) (((double) headerShadow / 100) * 255);
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, realHeaderValue);
                return true;

            case CUSTOM_HEADER_PROVIDER:
                String value = (String) newValue;
                Settings.System.putString(resolver,
                        Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER, value);
                int valueIndex = mHeaderProvider.findIndexOfValue(value);
                mHeaderProvider.setSummary(mHeaderProvider.getEntries()[valueIndex]);
                mDaylightHeaderPack.setEnabled(value.equals(mDaylightHeaderProvider));
                mHeaderBrowse.setTitle(valueIndex == 0 ? R.string.custom_header_browse_title : R.string.custom_header_pick_title);
                mHeaderBrowse.setSummary(valueIndex == 0 ? R.string.custom_header_browse_summary_new : R.string.custom_header_pick_summary);
                return true;

            case CUSTOM_HEADER_ENABLED:
                Boolean headerEnabled = (Boolean) newValue;
                updateHeaderProviderSummary(headerEnabled);
                return true;

            default:
                return false;
       }
    }

    private boolean isBrowseHeaderAvailable() {
        PackageManager pm = getActivity().getPackageManager();
        Intent browse = new Intent();
        browse.setClassName("org.omnirom.omnistyle", "org.omnirom.omnistyle.PickHeaderActivity");
        return pm.resolveActivity(browse, 0) != null;
    }

    private void getAvailableHeaderPacks(List<String> entries, List<String> values) {
        Map<String, String> headerMap = new HashMap<String, String>();
        Intent i = new Intent();
        PackageManager packageManager = getActivity().getPackageManager();
        i.setAction("org.omnirom.DaylightHeaderPack");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            String label = r.activityInfo.loadLabel(getActivity().getPackageManager()).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            headerMap.put(label, packageName);
        }
        i.setAction("org.omnirom.DaylightHeaderPack1");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            String label = r.activityInfo.loadLabel(getActivity().getPackageManager()).toString();
            if (r.activityInfo.name.endsWith(".theme")) {
                continue;
            }
            if (label == null) {
                label = packageName;
            }
            headerMap.put(label, packageName  + "/" + r.activityInfo.name);
        }
        List<String> labelList = new ArrayList<String>();
        labelList.addAll(headerMap.keySet());
        Collections.sort(labelList);
        for (String label : labelList) {
            entries.add(label);
            values.add(headerMap.get(label));
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.NITROGEN_SETTINGS;
    }

}
