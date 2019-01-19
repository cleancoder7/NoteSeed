package com.f0x1d.notes.fragment.settings;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import android.os.Handler;
import android.preference.Preference;

import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.f0x1d.notes.App;
import com.f0x1d.notes.R;
import com.f0x1d.notes.db.daos.NoteOrFolderDao;
import com.f0x1d.notes.db.entities.NoteItem;
import com.f0x1d.notes.db.entities.NoteOrFolder;
import com.f0x1d.notes.fragment.bottom_sheet.TextSizeDialog;
import com.f0x1d.notes.fragment.lock.СhoosePin;
import com.f0x1d.notes.fragment.settings.themes.ThemesFragment;
import com.f0x1d.notes.utils.SyncUtils;
import com.f0x1d.notes.utils.ThemesEngine;
import com.f0x1d.notes.utils.UselessUtils;
import com.f0x1d.notes.view.CenteredToolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class MainSettings extends PreferenceFragment {

    FragmentActivity myContext;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.settings, container, false);

        CenteredToolbar toolbar = v.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.settings));
        getActivity().setActionBar(toolbar);

        if (UselessUtils.ifCustomTheme()){
            getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(ThemesEngine.background));
            getActivity().getWindow().setStatusBarColor(ThemesEngine.statusBarColor);
            getActivity().getWindow().setNavigationBarColor(ThemesEngine.navBarColor);

            if (ThemesEngine.toolbarTransparent){
                toolbar.setBackgroundColor(ThemesEngine.toolbarColor);
            }
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
            View rootView = getView();
            if (rootView != null) {
                ListView list = (ListView) rootView.findViewById(android.R.id.list);
                list.setPadding(0, 0, 0, 0);
                list.setDivider(null);
            }
    }

    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("in_folder_back_stack", false).apply();

        addPreferencesFromResource(R.xml.settings);

        Preference import_db = findPreference("import");
            import_db.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SyncUtils.importFile(getActivity());
                    return false;
                }
            });

        Preference export = findPreference("export");
            export.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SyncUtils.export();
                    return false;
                }
            });

        Preference about = findPreference("about");
            about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    getActivity().getFragmentManager().beginTransaction().setCustomAnimations(R.animator.fade_in, R.animator.fade_out, R.animator.fade_in, R.animator.fade_out).replace(android.R.id.content, new AboutSettings(), "themes").addToBackStack(null).commit();
                    return false;
                }
            });

        Preference accent = findPreference("dayAccent");
        accent.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getActivity().getFragmentManager().beginTransaction().setCustomAnimations(R.animator.fade_in, R.animator.fade_out, R.animator.fade_in, R.animator.fade_out).replace(android.R.id.content, ThemesFragment.newInstance(true), "themes").addToBackStack(null).commit();
                return false;
            }
        });

        Preference fon = findPreference("fon");
        fon.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String[] themes = {getString(R.string.fon_standart), getString(R.string.fon_wallpaper)};

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setCancelable(false);
                builder.setSingleChoiceItems(themes, PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("fon", 0), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt("fon", 0).apply();
                                break;
                            case 1:
                                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt("fon", 1).apply();
                                break;
                        }
                    }
                });
                AlertDialog dialog =  builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt("fon", 0) == 1){
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                            builder1.setTitle(getString(R.string.wallpapers_ask));
                            builder1.setCancelable(false);
                            builder1.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("dark_fon", true).apply();
                                }
                            });
                            AlertDialog dialog1 =  builder1.setNeutralButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("dark_fon", false).apply();
                                }
                            }).create();

                            dialog1.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("night", false)){
                                        dialog1.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                                        dialog1.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(Color.BLACK);
                                    }

                                    if (UselessUtils.ifCustomTheme()){
                                        dialog1.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ThemesEngine.textColor);
                                        dialog1.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(ThemesEngine.textColor);

                                        dialog1.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
                                        dialog1.getButton(DialogInterface.BUTTON_NEUTRAL).setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
                                    }
                                }
                            });
                            dialog1.show();
                        }
                    }
                }).create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog0) {
                        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("night", false)){
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                        }
                        if (UselessUtils.ifCustomTheme()){
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ThemesEngine.textColor);

                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
                        }
                    }
                });
                dialog.show();
                return false;
            }
        });

        Preference textSize = findPreference("textSize");
        textSize.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                TextSizeDialog dialog1 = new TextSizeDialog();
                dialog1.show(myContext.getSupportFragmentManager(), "TAG");

                return false;
            }
        });

        Preference delete_all = findPreference("delete_all");
        delete_all.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                removeAll();
                return false;
            }
        });

        final SwitchPreference finger = (SwitchPreference) findPreference("finger");

        SwitchPreference lock = (SwitchPreference) findPreference("lock");
        lock.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("lock", false)){
                    getActivity().getFragmentManager().beginTransaction().setCustomAnimations(R.animator.fade_in, R.animator.fade_out, R.animator.fade_in, R.animator.fade_out).replace(android.R.id.content, new СhoosePin(), "choose_pin").addToBackStack(null).commit();
                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("pass", "").apply();
                }
                return false;
            }
        });

        if (Build.VERSION.SDK_INT >= 23){
            FingerprintManager fingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
            if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()){
                finger.setEnabled(false);
                finger.setSummary(getString(R.string.fingerprint_error2));
            } else if (!fingerprintManager.hasEnrolledFingerprints()){
                finger.setEnabled(false);
            }
        } else {
            finger.setEnabled(false);
            finger.setSummary(getString(R.string.fingerprint_error3));
        }
    }

    boolean delete = false;

    public void removeAll() {

        if (delete) {
            NoteOrFolderDao dao = App.getInstance().getDatabase().noteOrFolderDao();
            dao.nukeTable();
            dao.nukeTable2();

            Toast.makeText(getActivity(), "Success!", Toast.LENGTH_SHORT).show();
            return;
        }

        delete = true;
        Toast.makeText(getActivity(), R.string.one_more_time_to_delete, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                delete = false;
            }
        }, 2000);


    }

    public void openFile(String minmeType, int requestCode, Activity activity) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(minmeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.f0x1d.talkandtools.main.PICK_DATA");
        // if you want any file type, you can skip next line
        sIntent.putExtra("CONTENT_TYPE", minmeType);
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (activity.getPackageManager().resolveActivity(sIntent, 0) != null){
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open file");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { intent});
        }
        else {
            chooserIntent = Intent.createChooser(intent, "Open file");
        }

        try {
            MainSettings.this.startActivityForResult(chooserIntent, requestCode);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(activity, "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
        }
    }
}
