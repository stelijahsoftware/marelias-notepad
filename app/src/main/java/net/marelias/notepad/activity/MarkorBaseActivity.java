package net.marelias.notepad.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.Nullable;

import net.marelias.notepad.ApplicationObject;
import net.marelias.notepad.R;
import net.marelias.notepad.model.AppSettings;
import net.marelias.notepad.util.MarkorContextUtils;
import net.marelias.opoc.frontend.base.GsActivityBase;
import net.marelias.opoc.frontend.base.GsFragmentBase;

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
