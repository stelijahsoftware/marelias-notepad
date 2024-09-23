package net.gsantner.notepad2.activity;

import android.content.Context;

import androidx.annotation.Nullable;

import net.gsantner.notepad2.ApplicationObject;
import net.gsantner.notepad2.model.AppSettings;
import net.gsantner.notepad2.util.MarkorContextUtils;
import net.gsantner.opoc.frontend.base.GsFragmentBase;

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
