package app.michaelwuensch.bitbanana.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import app.michaelwuensch.bitbanana.util.RefConstants;
import app.michaelwuensch.bitbanana.R;
import app.michaelwuensch.bitbanana.licenses.LicensesActivity;


public class SupportFragment extends PreferenceFragmentCompat {

    private static final String LOG_TAG = SupportFragment.class.getSimpleName();

    private SwitchPreference mSwHideTotalBalance;
    private ListPreference mListCurrency;
    private Preference mPinPref;
    private ListPreference mListLanguage;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the settings from an XML resource
        setPreferencesFromResource(R.xml.support, rootKey);

        // Action when clicked on "need help"
        final Preference prefHelp = findPreference("help");
        prefHelp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String url = RefConstants.URL_HELP;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                return true;
            }
        });

        // Action when clicked on "reportBug"
        final Preference prefIssue = findPreference("reportIssue");
        prefIssue.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String url = RefConstants.URL_ISSUES;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                return true;
            }
        });


        // Action when clicked on "contribute"
        final Preference prefContribute = findPreference("contribute");
        prefContribute.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String url = RefConstants.URL_CONTRIBUTE;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                return true;
            }
        });

        // Action when clicked on "contribute"
        final Preference prefPrivacy = findPreference("privacy");
        prefPrivacy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String url = RefConstants.URL_PRIVACY;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                return true;
            }
        });

        // Action when clicked on "licenses"
        final Preference prefLicenses = findPreference("licenses");
        prefLicenses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), LicensesActivity.class));
                return true;
            }
        });
    }
}
