package net.marelias.notepad.activity;

import android.content.Context;

import androidx.annotation.Nullable;

import net.marelias.notepad.ApplicationObject;
import net.marelias.notepad.model.AppSettings;
import net.marelias.notepad.util.MarkorContextUtils;
import net.marelias.opoc.frontend.GsFragmentBase;

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
