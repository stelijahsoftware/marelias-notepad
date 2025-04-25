package org.marelias.notepad.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.Nullable;

import org.marelias.notepad.ApplicationObject;
import org.marelias.notepad.R;
import org.marelias.notepad.model.AppSettings;
import org.marelias.notepad.util.MarkorContextUtils;
import org.marelias.opoc.frontend.GsActivityBase;
import org.marelias.opoc.frontend.GsFragmentBase;

public abstract class MarkorBaseActivity extends GsActivityBase<AppSettings, MarkorContextUtils> {

    @Override
    protected void onPreCreate(@Nullable Bundle savedInstanceState) {
        super.onPreCreate(savedInstanceState);
        setTheme(R.style.AppTheme_Unified);
        _cu.setAppLanguage(this, _appSettings.getLanguage());
    }

    protected boolean onReceiveKeyPress(GsFragmentBase fragment, int keyCode, KeyEvent event) {
        return fragment.onReceiveKeyPress(keyCode, event);
    }

    @Override
    public AppSettings createAppSettingsInstance(Context applicationContext) {
        return ApplicationObject.settings();
    }

    @Override
    public MarkorContextUtils createContextUtilsInstance(Context applicationContext) {
        return new MarkorContextUtils(applicationContext);
    }

}
