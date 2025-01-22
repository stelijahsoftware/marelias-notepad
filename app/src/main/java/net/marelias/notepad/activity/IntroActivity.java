package net.marelias.notepad.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;

import net.marelias.notepad.ApplicationObject;
import net.marelias.notepad.R;
import net.marelias.opoc.util.GsContextUtils;

public class IntroActivity extends AppIntro {
    private static final String PREF_KEY_WAS_SHOWN = IntroActivity.class.getCanonicalName() + "was_shown";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GsContextUtils.instance.setAppLanguage(this, ApplicationObject.settings().getLanguage());

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest
        addSlide(AppIntroFragment.createInstance(getString(R.string.main_view), getString(R.string.notebook_is_the_home_of_your_files), 0, R.color.primary));

        // Permissions -- takes a permission and slide number
        setSkipButtonEnabled(false);
        setSwipeLock(false);
        setSeparatorColor(Color.DKGRAY);
        setIndicatorColor(GsContextUtils.instance.rcolor(this, R.color.accent), Color.LTGRAY);
    }

    @Override
    protected void onSkipPressed(@Nullable Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }

    @Override
    protected void onNextPressed(@Nullable Fragment currentFragment) {
        super.onNextPressed(currentFragment);
    }

    @Override
    protected void onDonePressed(@Nullable Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean(PREF_KEY_WAS_SHOWN, true).apply();
        finish();
    }

    @Override
    protected void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }
}
