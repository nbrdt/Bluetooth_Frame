package nfk.bluetooth.arduino.wetterverarbeitung;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * @author KI & Android Studio
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity implements ActivityResults {
    public static final String KEY_VIEW_GRAPHCOLOR = "draw_color";
    public static final String PREF_DEFAULTVALUE_VIEW_GRAPHCOLOR = "-65536";
    public static final String KEY_DATA_DELETEONFINISH = "delete_on_finish";
    public static final String KEY_DATA_SHOWVALUES = "show_values";
    public static final String PREF_DEFAULTVALUE_DATA_SHOWVALUES = "10000";
    public static final String KEY_CONNECTION_RECEIVERATE = "receive_data_rate";
    public static final String PREF_DEFAULTVALUE_CONNECTION_RECEIVERATE = "1000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    public void setErrorMessage(String message) {
        Intent data = new Intent("Closed on Error");
        data.putExtra(RESULTKEY_ERROR_MESSAGE, message);
        setResult(RESULT_ERROR, data);
    }

    @Override
    public void finishWithError(String message) {
        setErrorMessage(message);
        finish();
    }

    @Override
    public void showActivityError(Intent errorMessage) {
        Toast.makeText(this, errorMessage.getStringExtra(RESULTKEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference, String type) {
        // Set the listener to watch for value changes
        if (preference != null) {
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            // Trigger the listener immediately with the preference's
            // current value.
            switch (type) {
                case ("String"): {
                    sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                            PreferenceManager
                                    .getDefaultSharedPreferences(preference.getContext())
                                    .getString(preference.getKey(), null));
                    break;
                }
                case ("boolean"): {
                    sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                            PreferenceManager
                                    .getDefaultSharedPreferences(preference.getContext())
                                    .getBoolean(preference.getKey(), true));
                    break;
                }
                case ("int"): {
                    sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                            PreferenceManager
                                    .getDefaultSharedPreferences(preference.getContext())
                                    .getInt(preference.getKey(), -1));
                    break;
                }
                case ("long"): {
                    sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                            PreferenceManager
                                    .getDefaultSharedPreferences(preference.getContext())
                                    .getLong(preference.getKey(), -1));
                    break;
                }
                case ("float"): {
                    sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                            PreferenceManager
                                    .getDefaultSharedPreferences(preference.getContext())
                                    .getFloat(preference.getKey(), -1.0f));
                    break;
                }
                case ("Set<String>"): {
                    sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                            PreferenceManager
                                    .getDefaultSharedPreferences(preference.getContext())
                                    .getStringSet(preference.getKey(), new TreeSet<String>()));
                    break;
                }
            }
        }
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        bindPreferenceSummaryToValue(preference, "String");
    }


    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }


    /**
     * This fragment is used to Display closer information. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        private LinkedList<String> pm_changed;
        private SettingsActivity pm_host;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            pm_changed = new LinkedList<>();
            Bundle args = getArguments();
            pm_host = (SettingsActivity) getActivity();
            if (args != null) {
                String purpose = args.getString("use");
                if (purpose != null) {
                    switch (purpose) {
                        case ("view"): {
                            addPreferencesFromResource(R.xml.pref_view);
                            break;
                        }
                        case ("data"): {
                            addPreferencesFromResource(R.xml.pref_data);
                            break;
                        }
                        case ("connection"): {
                            addPreferencesFromResource(R.xml.pref_connection);
                            break;
                        }
                        default: {
                            defaultSetup();
                        }
                    }
                } else {
                    defaultSetup();
                }
            } else {
                defaultSetup();
            }
            setHasOptionsMenu(true);
        }

        /**
         * Called when the fragment is visible to the user and actively running.
         * This is generally
         * tied to Activity.onResume of the containing
         * Activity's lifecycle.
         */
        @Override
        public void onResume() {
            super.onResume();
            Bundle args = getArguments();
            if (args != null) {
                String purpose = args.getString("use");
                if (purpose != null) {
                    switch (purpose) {
                        case ("view"): {
                            bindPreferenceSummaryToValue(findPreference(KEY_VIEW_GRAPHCOLOR));
                            break;
                        }
                        case ("data"): {
                            bindPreferenceSummaryToValue(findPreference(KEY_DATA_DELETEONFINISH), "boolean");
                            bindPreferenceSummaryToValue(findPreference(KEY_DATA_SHOWVALUES));
                            break;
                        }
                        case ("connection"): {
                            bindPreferenceSummaryToValue(findPreference(KEY_CONNECTION_RECEIVERATE));
                            break;
                        }
                        default: {
                            defaultSummary();
                        }
                    }
                } else {
                    defaultSummary();
                }
            } else {
                defaultSummary();
            }
        }

        private void defaultSetup() {
            Log.w("Gen-Preference Fragment", "Had to use default setup");
            addPreferencesFromResource(R.xml.pref_connection);
        }

        private void defaultSummary() {
            Log.w("Gen-Preference Fragment", "Had to use default summary's");
            bindPreferenceSummaryToValue(findPreference(KEY_CONNECTION_RECEIVERATE));
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            pm_changed.add(preference.getKey());
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
