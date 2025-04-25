package org.marelias.notepad.activity;

import android.content.Context;

import androidx.annotation.Nullable;

import org.marelias.notepad.ApplicationObject;
import org.marelias.notepad.model.AppSettings;
import org.marelias.notepad.util.MarkorContextUtils;
import org.marelias.opoc.frontend.GsFragmentBase;

public abstract class MarkorBaseFragment extends GsFragmentBase<AppSettings, MarkorContextUtils> {
    @Nullable
    @Override
    public AppSettings createAppSettingsInstance(Context applicationContext) {
        return ApplicationObject.settings();
    }

    @Nullable
    @Override
    public MarkorContextUtils createContextUtilsInstance(Context applicationContext) {
        return new MarkorContextUtils(applicationContext);
    }
}
