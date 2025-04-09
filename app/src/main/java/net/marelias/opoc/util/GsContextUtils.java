/*#######################################################
 *
 * SPDX-FileCopyrightText: 2016-2024 Gregor Santner <gsantner AT mailbox DOT org>
 * SPDX-License-Identifier: Unlicense OR CC0-1.0
 *
 * Written 2016-2024 by Gregor Santner <gsantner AT mailbox DOT org>
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
#########################################################*/
package net.marelias.opoc.util;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.Html;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ScrollView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityManagerCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.core.os.ConfigurationCompat;
import androidx.core.text.TextUtilsCompat;
import androidx.core.view.ViewCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;

import net.marelias.opoc.opoc.GsCallback;
import net.marelias.opoc.opoc.GsTextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

@SuppressWarnings({"UnusedReturnValue", "rawtypes", "unused"})
public class GsContextUtils {
    //########################
    //## Constructor
    //########################
    public static final GsContextUtils instance = new GsContextUtils();

    public GsContextUtils() {
    }

    protected <T extends GsContextUtils> T thisp() {
        //noinspection unchecked
        return (T) this;
    }

    //########################
    //## Static fields & members
    //########################
    @SuppressLint("ConstantLocale")
    public final static Locale INITIAL_LOCALE = Locale.getDefault();
    public final static String EXTRA_FILEPATH = "EXTRA_FILEPATH";
    public final static String PREF_KEY__SAF_TREE_URI = "pref_key__saf_tree_uri";
    public final static String CONTENT_RESOLVER_FILE_PROXY_SEGMENT = "CONTENT_RESOLVER_FILE_PROXY_SEGMENT";

    public final static int REQUEST_SAF = 50003;
    public final static int REQUEST_STORAGE_PERMISSION_M = 50004;
    public final static int REQUEST_STORAGE_PERMISSION_R = 50005;
    private final static int BLINK_ANIMATOR_TAG = -1206813720;

    public static int TEXTFILE_OVERWRITE_MIN_TEXT_LENGTH = 2;
    protected static String m_chooserTitle = "➥";


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //########################
    //## Resources
    //########################
    public enum ResType {
        ID, BOOL, INTEGER, COLOR, STRING, ARRAY, DRAWABLE, PLURALS,
        ANIM, ATTR, DIMEN, LAYOUT, MENU, RAW, STYLE, XML,
    }

    /**
     * Find out the numerical resource id by given {@link ResType}
     *
     * @return A valid id if the id could be found, else 0
     */
    @SuppressLint("DiscouragedApi")
    public int getResId(final Context context, final ResType resType, String name) {
        try {
            name = name.toLowerCase(Locale.ROOT).replace("#", "no").replaceAll("[^A-Za-z0-9_]", "_");
            return context.getResources().getIdentifier(name, resType.name().toLowerCase(Locale.ENGLISH), context.getPackageName());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get String by given string resource id (numeric)
     */
    public String rstr(Context context, @StringRes final int strResId) {
        try {
            return context.getString(strResId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get String by given string resource identifier (textual)
     */
    public String rstr(final Context context, final String strResKey, Object... a0getResKeyAsFallback) {
        try {
            final String s = rstr(context, getResId(context, ResType.STRING, strResKey));
            if (s != null) {
                return s;
            }
        } catch (Exception ignored) {
        }
        return a0getResKeyAsFallback != null && a0getResKeyAsFallback.length > 0 ? strResKey : null;
    }

    /**
     * Get drawable from given resource identifier
     */
    public Drawable rdrawable(final Context context, @DrawableRes final int resId) {
        try {
            return ContextCompat.getDrawable(context, resId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get color by given color resource id
     */
    @ColorInt
    public int rcolor(final Context context, @ColorRes final int resId) {
        if (context == null || resId == 0) {
            Log.e(getClass().getName(), "GsContextUtils::rcolor: resId is 0!");
            return Color.BLACK;
        }
        return ContextCompat.getColor(context, resId);
    }

    public String getAppVersionName(final Context context) {
        final PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getAppIdFlavorSpecific(context), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            try {
                PackageInfo info = manager.getPackageInfo(getAppIdUsedAtManifest(context), 0);
                return info.versionName;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return "?";
    }

    /**
     * Get the apps base packagename, which is equal with all build flavors and variants
     */
    public String getAppIdUsedAtManifest(final Context context) {
        String pkg = rstr(context, "manifest_package_id");
        return !TextUtils.isEmpty(pkg) ? pkg : context.getPackageName();
    }

    /**
     * Get this apps package name, returns the flavor specific package name.
     */
    public String getAppIdFlavorSpecific(final Context context) {
        return context.getPackageName();
    }

    /**
     * Get field from ${applicationId}.BuildConfig
     * May be helpful in libraries, where a access to
     * BuildConfig would only get values of the library
     * rather than the app ones. It awaits a string resource
     * of the package set in manifest (root element).
     * Falls back to applicationId of the app which may differ from manifest.
     */
    public Object getBuildConfigValue(final Context context, final String fieldName) {
        final String pkg = getAppIdUsedAtManifest(context) + ".BuildConfig";
        try {
            Class<?> c = Class.forName(pkg);
            return c.getField(fieldName).get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get an {@link Locale} out of a android language code
     * The {@code androidLC} may be in any of the forms: de, en, de-rAt
     */
    public Locale getLocaleByAndroidCode(String androidLC) {
        if (!TextUtils.isEmpty(androidLC)) {
            return androidLC.contains("-r")
                    ? new Locale(androidLC.substring(0, 2), androidLC.substring(4, 6)) // de-rAt
                    : new Locale(androidLC); // de
        }
        return Resources.getSystem().getConfiguration().locale;
    }

    /**
     * Set the apps language
     * {@code androidLC} may be in any of the forms: en, de, de-rAt
     * If given an empty string, the default (system) locale gets loaded
     */
    public <T extends GsContextUtils> T setAppLanguage(final Context context, final String androidLC) {
        Locale locale = getLocaleByAndroidCode(androidLC);
        locale = (locale != null && !androidLC.isEmpty()) ? locale : Resources.getSystem().getConfiguration().locale;
        setAppLocale(context, locale);
        return thisp();
    }

    public <T extends GsContextUtils> T setAppLocale(final Context context, final Locale locale) {
        Configuration config = context.getResources().getConfiguration();
        config.locale = (locale != null ? locale : Resources.getSystem().getConfiguration().locale);
        context.getResources().updateConfiguration(config, null);
        //noinspection ConstantConditions
        Locale.setDefault(locale);
        return thisp();
    }

    /**
     * Send a {@link Intent#ACTION_VIEW} Intent with given parameter
     * If the parameter is an string a browser will get triggered
     */
    public <T extends GsContextUtils> T openWebpageInExternalBrowser(final Context context, final String url) {
        try {
            startActivity(context, new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return thisp();
    }

    /**
     * Convert a html string to an android {@link Spanned} object
     */
    public Spanned htmlToSpanned(final String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    /**
     * Convert android dp unit to pixel unit
     */
    public int convertDpToPx(final Context context, final float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    /**
     * Get public (accessible) appdata folders
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public List<Pair<File, String>> getAppDataPublicDirs(final Context context, boolean internalStorageFolder, boolean sdcardFolders, boolean storageNameWithoutType) {
        List<Pair<File, String>> dirs = new ArrayList<>();
        for (File externalFileDir : ContextCompat.getExternalFilesDirs(context, null)) {
            if (externalFileDir == null || Environment.getExternalStorageDirectory() == null) {
                continue;
            }
            boolean isInt = externalFileDir.getAbsolutePath().startsWith(Environment.getExternalStorageDirectory().getAbsolutePath());
            boolean add = (internalStorageFolder && isInt) || (sdcardFolders && !isInt);
            if (add) {
                dirs.add(new Pair<>(externalFileDir, getStorageName(externalFileDir, storageNameWithoutType)));
                if (!externalFileDir.exists() && externalFileDir.mkdirs()) ;
            }
        }
        return dirs;
    }

    public String getStorageName(final File externalFileDir, final boolean storageNameWithoutType) {
        boolean isInt = externalFileDir.getAbsolutePath().startsWith(Environment.getExternalStorageDirectory().getAbsolutePath());

        String[] split = externalFileDir.getAbsolutePath().split("/");
        if (split.length > 2) {
            return isInt ? (storageNameWithoutType ? "Internal Storage" : "") : (storageNameWithoutType ? split[2] : ("SD Card (" + split[2] + ")"));
        } else {
            return "Storage";
        }
    }

    public List<Pair<File, String>> getStorages(final Context context, final boolean internalStorageFolder, final boolean sdcardFolders) {
        List<Pair<File, String>> storages = new ArrayList<>();
        for (Pair<File, String> pair : getAppDataPublicDirs(context, internalStorageFolder, sdcardFolders, true)) {
            if (pair.first != null && pair.first.getAbsolutePath().lastIndexOf("/Android/data") > 0) {
                try {
                    storages.add(new Pair<>(new File(pair.first.getCanonicalPath().replaceFirst("/Android/data.*", "")), pair.second));
                } catch (IOException ignored) {
                }
            }
        }
        return storages;
    }

    public File getStorageRootFolder(final Context context, final File file) {
        String filepath;
        try {
            filepath = file.getCanonicalPath();
        } catch (Exception ignored) {
            return null;
        }
        for (Pair<File, String> storage : getStorages(context, false, true)) {
            if (filepath.startsWith(storage.first.getAbsolutePath())) {
                return storage.first;
            }
        }
        return null;
    }

    /**
     * Try to tint all {@link Menu}s {@link MenuItem}s with given color
     */
    public void tintMenuItems(final Menu menu, final boolean recurse, @ColorInt final int iconColor) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            try {
                tintDrawable(item.getIcon(), iconColor);
                if (item.hasSubMenu() && recurse) {
                    //noinspection ConstantConditions
                    tintMenuItems(item.getSubMenu(), recurse, iconColor);
                }
            } catch (Exception ignored) {
                // This should not happen at all, but may in bad menu.xml configuration
            }
        }
    }

    /**
     * Loads {@link Drawable} by given {@link DrawableRes} and applies a color
     */
    public Drawable tintDrawable(final Context context, @DrawableRes final int drawableRes, @ColorInt final int color) {
        return tintDrawable(rdrawable(context, drawableRes), color);
    }

    /**
     * Tint a {@link Drawable} with given {@code color}
     */
    public Drawable tintDrawable(@Nullable Drawable drawable, @ColorInt final int color) {
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable.mutate(), color);
        }
        return drawable;
    }

    /**
     * Try to make icons in Toolbar/ActionBars SubMenus visible
     * This may not work on some devices and it maybe won't work on future android updates
     */
    public void setSubMenuIconsVisibility(final Menu menu, final boolean visible) {
        if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL) {
            return;
        }
        if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
            try {
                @SuppressLint("PrivateApi") Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                m.setAccessible(true);
                m.invoke(menu, visible);
            } catch (Exception ignored) {
                Log.d(getClass().getName(), "Error: 'setSubMenuIconsVisibility' not supported on this device");
            }
        }
    }

    /**
     * A {@link InputFilter} for filenames
     */
    @SuppressWarnings({"UnnecessaryLocalVariable", "RedundantSuppression"})
    public InputFilter makeFilenameInputFilter() {
        return (filterSrc, filterStart, filterEnd, filterDest, filterDstart, filterDend) -> {
            if (filterSrc != null && filterSrc.length() > 0) {
                final String newInput = filterSrc.subSequence(filterStart, filterEnd).toString().replace(" ", "");
                final String newInputFiltered = GsFileUtils.getFilteredFilenameWithoutDisallowedChars(newInput);
                if (!newInput.equals(newInputFiltered)) {
                    return "";
                }
            }
            return null;
        };
    }

    public String getMimeType(final Context context, final File file) {
        return getMimeType(context, file.getAbsolutePath());
    }

    /**
     * Detect MimeType of given file
     */
    public String getMimeType(final Context context, String uri) {
        String mimeType;
        uri = uri.replaceFirst("\\.jenc$", "");
        if (uri.startsWith(ContentResolver.SCHEME_CONTENT + "://")) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(Uri.parse(uri));
        } else {
            String ext = MimeTypeMap.getFileExtensionFromUrl(uri);
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
        }

        // Next-best try if other methods fail
        if (GsTextUtils.isNullOrEmpty(mimeType) && new File(uri).exists()) {
            mimeType = GsFileUtils.getMimeType(new File(uri));
        }

        if (GsTextUtils.isNullOrEmpty((mimeType))) {
            mimeType = "*/*";
        }
        return mimeType.toLowerCase(Locale.ROOT);
    }

    public boolean isDeviceGoodHardware(final Context context) {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            return !ActivityManagerCompat.isLowRamDevice(activityManager) &&
                    Runtime.getRuntime().availableProcessors() >= 4 &&
                    activityManager.getMemoryClass() >= 128;
        } catch (Exception ignored) {
            return true;
        }
    }

    // Get all of providers of the current app
    public List<ProviderInfo> getProvidersInfos(final Context context) {
        final List<ProviderInfo> providers = new ArrayList<>();
        for (final ProviderInfo info : context.getPackageManager().queryContentProviders(null, 0, 0)) {
            if (info.applicationInfo.uid == context.getApplicationInfo().uid) {
                providers.add(info);
            }
        }
        return providers;
    }

    public String getFileProvider(final Context context) {
        for (final ProviderInfo info : getProvidersInfos(context)) {
            if (info.name.matches("(?i).*fileprovider.*")) {
                return info.authority;
            }
        }
        throw new RuntimeException("Error at GsContextUtils::getFileProviderAuthority(context): No FileProvider authority setup");
    }

    /**
     * Animate to specified Activity
     *
     * @param to                 The class of the activity
     * @param finishFromActivity true: Finish the current activity
     * @param requestCode        Request code for stating the activity, not waiting for result if null
     */
    public <T extends GsContextUtils> T animateToActivity(final Activity context, final Class to,
                                                          final Boolean finishFromActivity, final Integer requestCode) {
        return animateToActivity(context, new Intent(context, to), finishFromActivity, requestCode);
    }

    /**
     * Animate to Activity specified in intent
     * Requires animation resources
     *
     * @param intent             Intent to open start an activity
     * @param finishFromActivity true: Finish the current activity
     * @param requestCode        Request code for stating the activity, not waiting for result if null
     */
    public <T extends GsContextUtils> T animateToActivity(final Activity context, final Intent intent,
                                                          final Boolean finishFromActivity, final Integer requestCode) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        if (requestCode != null) {
            context.startActivityForResult(intent, requestCode);
        } else {
            context.startActivity(intent);
        }
        context.overridePendingTransition(getResId(context, ResType.DIMEN, "fadein"), getResId(context, ResType.DIMEN, "fadeout"));
        if (finishFromActivity != null && finishFromActivity) {
            context.finish();
        }
        return thisp();
    }


    public <T extends GsContextUtils> T setChooserTitle(final String title) {
        m_chooserTitle = title;
        return thisp();
    }

    /**
     * Allow to choose a handling app for given intent
     *
     * @param intent      Thing to be shared
     * @param chooserText The title text for the chooser, or null for default
     */
    public void showChooser(final Context context, final Intent intent, final String chooserText) {
        try {
            startActivity(context, Intent.createChooser(intent, chooserText != null ? chooserText : m_chooserTitle));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Try to create a new desktop shortcut on the launcher. Add permissions:
     * <uses-permission android:name="android.permission.INSTALL_SHORTCUT" />
     * <uses-permission android:name="com.android.launcher.permission.INSTALL_SHORTCUT" />
     *
     * @param intent  The intent to be invoked on tap
     * @param iconRes Icon resource for the item
     * @param title   Title of the item
     */
    public void createLauncherDesktopShortcut(final Context context, final Intent intent, @DrawableRes final int iconRes, final String title) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (intent.getAction() == null) {
            intent.setAction(Intent.ACTION_VIEW);
        }

        ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(context, Long.toString(new Random().nextLong()))
                .setIntent(intent)
                .setIcon(IconCompat.createWithResource(context, iconRes))
                .setShortLabel(title)
                .setLongLabel(title)
                .build();
        ShortcutManagerCompat.requestPinShortcut(context, shortcut, null);
    }

    /**
     * Share the given files as stream with given mime-type
     *
     * @param files    The files to share
     * @param mimeType The files mime type. Usally * / * is the best option
     */
    public boolean shareStreamMultiple(final Context context, final Collection<File> files, final String mimeType) {
        ArrayList<Uri> uris = new ArrayList<>();
        for (File file : files) {
            File uri = new File(file.toString());
            uris.add(FileProvider.getUriForFile(context, getFileProvider(context), file));
        }

        try {
            final Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setType(mimeType);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // = works without Activity context
            showChooser(context, intent, null);
            return true;
        } catch (Exception e) { // FileUriExposed(API24) / IllegalArgument
            return false;
        }
    }

    /**
     * Start activity specified by Intent. Add FLAG_ACTIVITY_NEW_TASK in case passed context is not a {@link Activity}
     * (when a non-Activity {@link Context} is passed a Exception is thrown otherwise)
     *
     * @param context Context, preferably a Activity
     * @param intent  Intent
     */
    public void startActivity(final Context context, final Intent intent) {
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        context.startActivity(intent);
    }

    /**
     * Open a View intent for given file
     *
     * @param file The file to share
     */
    public boolean viewFileInOtherApp(final Context context, final File file, @Nullable final String type) {
        // On some specific devices the first won't work
        Uri fileUri = null;
        try {
            fileUri = FileProvider.getUriForFile(context, getFileProvider(context), file);
        } catch (Exception ignored) {
            try {
                fileUri = Uri.fromFile(file);
            } catch (Exception ignored2) {
            }
        }

        if (fileUri != null) {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, (TextUtils.isEmpty(type) ? getMimeType(context, file) : type));
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.setClipData(ClipData.newRawUri(file.getName(), fileUri));
            intent.putExtra(EXTRA_FILEPATH, file.getAbsolutePath());
            intent.putExtra(Intent.EXTRA_TITLE, file.getName());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivity(context, intent);
            return true;
        }
        return false;
    }

    /***
     * Replace (primary) clipboard contents with given {@code text}
     * @param text Text to be set
     */
    public boolean setClipboard(final Context context, final CharSequence text) {
        try {
            final ClipboardManager cm = ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE));
            ClipData clip = ClipData.newPlainText(context.getPackageName(), text);
            cm.setPrimaryClip(clip);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static File checkPath(final String path) {
        final File f;
        return (!TextUtils.isEmpty(path) && (f = new File(path)).canRead()) ? f : null;
    }

    /**
     * Try to force extract a absolute filepath from an intent
     *
     * @param receivingIntent The intent from {@link Activity#getIntent()}
     * @return A file or null if extraction did not succeed
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    public File extractFileFromIntent(final Context context, final Intent receivingIntent) {
        final String action = receivingIntent.getAction();
        final String type = receivingIntent.getType();
        final String extPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String tmps;
        String fileStr;
        File result = null;

        if ((Intent.ACTION_VIEW.equals(action) || Intent.ACTION_EDIT.equals(action)) || Intent.ACTION_SEND.equals(action)) {

            // Màrkor, SimpleMobileTools FileManager
            if (receivingIntent.hasExtra((tmps = EXTRA_FILEPATH))) {
                result = checkPath(receivingIntent.getStringExtra(tmps));
            }

            // Analyze data/Uri
            Uri fileUri = receivingIntent.getData();
            fileUri = (fileUri != null ? fileUri : receivingIntent.getParcelableExtra(Intent.EXTRA_STREAM));
            if (result == null && fileUri != null && (fileStr = fileUri.toString()) != null) {
                // Uri contains file
                if (fileStr.startsWith("file://")) {
                    result = checkPath(fileUri.getPath());
                }

                if (fileStr.startsWith((tmps = "content://"))) {
                    fileStr = fileStr.substring(tmps.length());
                    String fileProvider = fileStr.substring(0, fileStr.indexOf("/"));
                    fileStr = fileStr.substring(fileProvider.length() + 1);

                    // Some file managers dont add leading slash
                    if (fileStr.startsWith("storage/")) {
                        fileStr = "/" + fileStr;
                    }
                    // Some do add some custom prefix
                    for (String prefix : new String[]{"file", "document", "root_files", "name"}) {
                        if (fileStr.startsWith(prefix)) {
                            fileStr = fileStr.substring(prefix.length());
                        }
                    }

                    // prefix for External storage (/storage/emulated/0  ///  /sdcard/) --> e.g. "content://com.amaze.filemanager/storage_root/file.txt" = "/sdcard/file.txt"
                    for (String prefix : new String[]{"external/", "media/", "storage_root/", "external-path/"}) {
                        if (result == null && fileStr.startsWith((tmps = prefix))) {
                            result = checkPath(Uri.decode(extPath + "/" + fileStr.substring(tmps.length())));
                        }
                    }

                    // Next/OwnCloud Fileprovider
                    for (String fp : new String[]{"org.nextcloud.files", "org.nextcloud.beta.files", "org.owncloud.files"}) {
                        if (result == null && fileProvider.equals(fp) && fileStr.startsWith(tmps = "external_files/")) {
                            result = checkPath(Uri.decode("/storage/" + fileStr.substring(tmps.length()).trim()));
                        }
                    }

                    // AOSP File Manager/Documents
                    if (result == null && fileProvider.equals("com.android.externalstorage.documents") && fileStr.startsWith(tmps = "/primary%3A")) {
                        result = checkPath(Uri.decode(extPath + "/" + fileStr.substring(tmps.length())));
                    }

                    // Mi File Explorer
                    if (result == null && fileProvider.equals("com.mi.android.globalFileexplorer.myprovider") && fileStr.startsWith(tmps = "external_files")) {
                        result = checkPath(Uri.decode(extPath + fileStr.substring(tmps.length())));
                    }

                    if (result == null && fileStr.startsWith(tmps = "external_files/")) {
                        for (String prefix : new String[]{extPath, "/storage", ""}) {
                            if (result == null) {
                                result = checkPath(Uri.decode(prefix + "/" + fileStr.substring(tmps.length())));
                            }
                        }
                    }

                    // URI Encoded paths with full path after content://package/
                    if (result == null && fileStr.startsWith("/") || fileStr.startsWith("%2F")) {
                        result = checkPath(Uri.decode(fileStr));
                        if (result == null) {
                            result = checkPath(fileStr);
                        }
                    }
                }
            }

            fileUri = receivingIntent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (result == null && fileUri != null && !TextUtils.isEmpty(tmps = fileUri.getPath()) && tmps.startsWith("/")) {
                result = checkPath(tmps);
            }

            // Scan MediaStore.MediaColumns
            final String[] sarr = contentColumnData(context, receivingIntent, MediaStore.MediaColumns.DATA, (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? MediaStore.MediaColumns.DATA : null));
            if (result == null && sarr[0] != null) {
                result = checkPath(sarr[0]);
            }

            if (result == null && sarr[1] != null) {
                result = checkPath(Environment.getExternalStorageDirectory() + "/" + sarr[1]);
            }
        }

        // Try build proxy by ContentResolver if no file found
        if (result == null) {
            try {
                // Try detect content file & filename in Intent
                Uri uri = new ShareCompat.IntentReader(context, receivingIntent).getStream();
                uri = (uri != null ? uri : receivingIntent.getData());
                final String[] sarr = contentColumnData(context, receivingIntent, OpenableColumns.DISPLAY_NAME);
                tmps = sarr != null && !TextUtils.isEmpty(sarr[0]) ? sarr[0] : uri.getLastPathSegment();

                // Proxy file to app-private storage (= java.io.File)
                File f = new File(context.getCacheDir(), CONTENT_RESOLVER_FILE_PROXY_SEGMENT + "/" + tmps);
                f.getParentFile().mkdirs();
                byte[] data = GsFileUtils.readCloseBinaryStream(context.getContentResolver().openInputStream(uri));
                GsFileUtils.writeFile(f, data, null);
                f.setReadable(true);
                f.setWritable(true);
                result = checkPath(f.getAbsolutePath());
            } catch (Exception ignored) {
            }
        }

        return result;
    }

    public static String[] contentColumnData(final Context context, final Intent intent, final String... columns) {
        final String[] out = (new String[columns.length]);
        final int INVALID = -1;
        Cursor cursor;
        try {
            cursor = context.getContentResolver().query(intent.getData(), columns, null, null, null);
        } catch (Exception ignored) {
            cursor = null;
        }
        if (cursor != null && cursor.moveToFirst()) {
            for (int i = 0; i < columns.length; i++) {
                final int coli = TextUtils.isEmpty(columns[i]) ? INVALID : cursor.getColumnIndex(columns[i]);
                out[i] = (coli == INVALID ? null : cursor.getString(coli));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return out;
    }

    /**
     * Extract result data from {@link Activity}.onActivityResult.
     * Forward all arguments from context. Only requestCodes as implemented in {@link GsContextUtils} are analyzed.
     * Also may forward results via callback
     */
    @SuppressLint("ApplySharedPref")
    public void extractResultFromActivityResult(final Activity context, final int requestCode, final int resultCode, final Intent intent) {
        switch (requestCode) {
            case REQUEST_SAF: {
                if (resultCode == Activity.RESULT_OK && intent != null && intent.getData() != null) {
                    final Uri treeUri = intent.getData();
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_KEY__SAF_TREE_URI, treeUri.toString()).commit();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        final ContentResolver resolver = context.getContentResolver();
                        try {
                            resolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        } catch (SecurityException se) {
                            resolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                    }
                }
                break;
            }
            case REQUEST_STORAGE_PERMISSION_M:
            case REQUEST_STORAGE_PERMISSION_R: {
                checkExternalStoragePermission(context);
                break;
            }
        }
    }

    /***
     * Request storage access. The user needs to press "Select storage" at the correct storage.
     * @param context The {@link Activity} which will receive the result from startActivityForResult
     */
    public void requestStorageAccessFramework(final Activity context) {
        if (context != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            );
            context.startActivityForResult(intent, REQUEST_SAF);
        }
    }

    /**
     * Get storage access framework tree uri. The user must have granted access via {@link #requestStorageAccessFramework(Activity)}
     *
     * @return Uri or null if not granted yet
     */
    public Uri getStorageAccessFrameworkTreeUri(final Context context) {
        String treeStr = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_KEY__SAF_TREE_URI, null);
        if (!TextUtils.isEmpty(treeStr)) {
            try {
                return Uri.parse(treeStr);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * Get mounted storage folder root (by tree uri). The user must have granted access via {@link #requestStorageAccessFramework(Activity)}
     *
     * @return File or null if SD not mounted
     */
    public File getStorageAccessFolder(final Context context) {
        Uri safUri = getStorageAccessFrameworkTreeUri(context);
        if (safUri != null) {
            String safUriStr = safUri.toString();
            for (Pair<File, String> storage : getStorages(context, false, true)) {
                String storageFolderName = storage.first.getName();
                if (safUriStr.contains(storageFolderName)) {
                    return storage.first;
                }
            }
        }
        return null;
    }

    /**
     * Check whether or not a file is under a storage access folder (external storage / SD)
     *
     * @param file The file object (file/folder)
     * @return Whether or not the file is under storage access folder
     */
    public boolean isUnderStorageAccessFolder(final Context context, final File file, boolean isDir) {
        if (file != null) {
            isDir = isDir || (file.exists() && file.isDirectory());
            // When file writeable as is, it's the fastest way to learn SAF isn't required
            if (canWriteFile(context, file, isDir, false)) {
                return false;
            }
            for (Pair<File, String> storage : getStorages(context, false, true)) {
                if (file.getAbsolutePath().startsWith(storage.first.getAbsolutePath())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isContentResolverProxyFile(final File file) {
        return file != null && file.getParentFile() != null && CONTENT_RESOLVER_FILE_PROXY_SEGMENT.equals(file.getParentFile().getName());
    }

    public Collection<File> getCacheDirs(final Context context) {
        final Set<File> dirs = new HashSet<>();
        dirs.add(context.getCacheDir());
        dirs.add(context.getExternalCacheDir());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            dirs.addAll(Arrays.asList(context.getExternalCacheDirs()));
        }
        dirs.removeAll(Collections.singleton(null));
        return dirs;
    }

    /**
     * Check whether or not a file can be written.
     * Requires storage access framework permission for external storage (SD)
     *
     * @param file  The file object (file/folder)
     * @param isDir Whether or not the given file parameter is a directory
     * @return Whether or not the file can be written
     */
    public boolean canWriteFile(final Context context, final File file, final boolean isDir, final boolean trySaf) {
        if (file == null) {
            return false;
        }

        // Try direct file access
        if (GsFileUtils.canCreate(file)) {
            return true;
        }

        // Own AppData directories do not require any special permission or handling
        if (GsCollectionUtils.any(getCacheDirs(context), f -> GsFileUtils.isChild(f, file))) {
            return true;
        }

        if (trySaf) {
            final DocumentFile dof = getDocumentFile(context, file, isDir);
            return dof != null && dof.canWrite();
        }

        return false;
    }

    /**
     * Get a {@link DocumentFile} object out of a normal java {@link File}.
     * When used on a external storage (SD), use {@link #requestStorageAccessFramework(Activity)}
     * first to get access. Otherwise this will fail.
     *
     * @param file  The file/folder to convert
     * @param isDir Whether or not file is a directory. For non-existing (to be created) files this info is not known hence required.
     * @return A {@link DocumentFile} object or null if file cannot be converted
     */
    @SuppressWarnings("RegExpRedundantEscape")
    public DocumentFile getDocumentFile(final Context context, final File file, final boolean isDir) {
        // On older versions use fromFile
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return DocumentFile.fromFile(file);
        }

        // Find storage root folder
        File baseFolderFile = getStorageRootFolder(context, file);
        String baseFolder = baseFolderFile == null ? null : baseFolderFile.getAbsolutePath();
        boolean originalDirectory = false;
        if (baseFolder == null) {
            return null;
        }

        String relPath = null;
        try {
            String fullPath = file.getCanonicalPath();
            if (!baseFolder.equals(fullPath)) {
                relPath = fullPath.substring(baseFolder.length() + 1);
            } else {
                originalDirectory = true;
            }
        } catch (IOException e) {
            return null;
        } catch (Exception ignored) {
            originalDirectory = true;
        }
        Uri treeUri;
        if ((treeUri = getStorageAccessFrameworkTreeUri(context)) == null) {
            return null;
        }
        DocumentFile dof = DocumentFile.fromTreeUri(context, treeUri);
        if (originalDirectory) {
            return dof;
        }
        String[] parts = relPath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
            @SuppressWarnings("ConstantConditions")
            DocumentFile nextDof = dof.findFile(parts[i]);
            if (nextDof == null) {
                try {
                    nextDof = ((i < parts.length - 1) || isDir) ? dof.createDirectory(parts[i]) : dof.createFile("image", parts[i]);
                } catch (Exception ignored) {
                }
            }
            dof = nextDof;
        }
        return dof;
    }

    public void showMountSdDialog(final Activity context, @StringRes final int title, @StringRes final int description,
                                  @DrawableRes final int mountDescriptionGraphic) {
        // Image viewer
        ImageView imv = new ImageView(context);
        imv.setImageResource(mountDescriptionGraphic);
        imv.setAdjustViewBounds(true);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setView(imv);
        dialog.setTitle(title);
        dialog.setMessage(context.getString(description) + "\n\n");
        dialog.setNegativeButton(android.R.string.cancel, null);
        dialog.setPositiveButton(android.R.string.yes, (dialogInterface, i) -> requestStorageAccessFramework(context));
        AlertDialog dialogi = dialog.create();
        dialogi.show();
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "StatementWithEmptyBody"})
    public void writeFile(final Activity context, final File file, final boolean isDirectory, final GsCallback.a2<Boolean, OutputStream> writeFileCallback) {
        try {
            OutputStream fileOutputStream = null;
            ParcelFileDescriptor pfd = null;
            final boolean existingEmptyFile = file.canWrite() && file.length() < TEXTFILE_OVERWRITE_MIN_TEXT_LENGTH;
            final boolean nonExistingCreatableFile = !file.exists() && file.getParentFile() != null && file.getParentFile().canWrite();
            if (isContentResolverProxyFile(file)) {
                // File initially read from Activity, Intent & ContentResolver -> write back to it
                try {
                    Intent intent = context.getIntent();
                    Uri uri = new ShareCompat.IntentReader(context, intent).getStream();
                    uri = (uri != null ? uri : intent.getData());
                    fileOutputStream = context.getContentResolver().openOutputStream(uri, "rwt");
                } catch (Exception ignored) {
                }
            } else if (existingEmptyFile || nonExistingCreatableFile) {
                if (isDirectory) {
                    file.mkdirs();
                } else {
                    fileOutputStream = new FileOutputStream(file);
                }
            } else {
                DocumentFile dof = getDocumentFile(context, file, isDirectory);
                if (dof != null && dof.canWrite()) {
                    if (isDirectory) {
                        // Nothing to do
                    } else {
                        pfd = context.getContentResolver().openFileDescriptor(dof.getUri(), "rwt");
                        fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                    }
                }
            }
            if (writeFileCallback != null) {
                writeFileCallback.callback(fileOutputStream != null || (isDirectory && file.exists()), fileOutputStream);
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (Exception ignored) {
                }
            }
            if (pfd != null) {
                pfd.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param locale   {@link Locale} locale
     * @param format   {@link String} text which 'll be used as format for {@link SimpleDateFormat}
     * @param datetime {@link Long}   requested time miliseconds
     * @param fallback {@link String} default fallback value. If the format is incorrect and a default is not provided, return the specified format
     * @return formatted string
     */
    public String formatDateTime(@Nullable final Locale locale, @NonNull final String format, @Nullable final Long datetime,
                                 @Nullable final String... fallback) {
        try {
            final Locale l = locale != null ? locale : Locale.getDefault();
            final long t = datetime != null ? datetime : System.currentTimeMillis();
            return new SimpleDateFormat(GsTextUtils.unescapeString(format), l).format(t);
        } catch (Exception err) {
            return (fallback != null && fallback.length > 0) ? fallback[0] : format;
        }
    }

    public String formatDateTime(@Nullable final Context context, @NonNull final String format, @Nullable final Long datetime, @Nullable final String... def) {
        Locale locale = null;
        if (context != null) {
            locale = ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0);
        }
        return formatDateTime(locale, format, datetime, def);
    }

    public void requestExternalStoragePermission(final Activity activity) {
        final int v = android.os.Build.VERSION.SDK_INT;

        if (v >= Build.VERSION_CODES.R) {
            try {
                final Uri uri = Uri.parse("package:" + getAppIdFlavorSpecific(activity));
                final Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                activity.startActivityForResult(intent, REQUEST_STORAGE_PERMISSION_R);
            } catch (final Exception ex) {
                final Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivityForResult(intent, REQUEST_STORAGE_PERMISSION_R);
            }
        }

        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION_M);
    }

    @SuppressWarnings("ConstantConditions")
    public boolean checkExternalStoragePermission(final Context context) {
        final int v = android.os.Build.VERSION.SDK_INT;

        // Android R Manage-All-Files permission
        if (v >= android.os.Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }

        // Android M permissions
        if (v >= android.os.Build.VERSION_CODES.M && v < android.os.Build.VERSION_CODES.R) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        // In case unsure, check if anything is writable at external storage
        for (final File f : Environment.getExternalStorageDirectory() != null ? Environment.getExternalStorageDirectory().listFiles() : new File[0]) {
            if (f.canWrite()) {
                return true;
            }
        }

        return false;
    }

    public <T extends GsContextUtils> T showSoftKeyboard(final Activity activity, final boolean show, final View... view) {
        if (activity != null) {
            final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            final View focus = (view != null && view.length > 0) ? view[0] : activity.getCurrentFocus();
            final IBinder token = focus != null ? focus.getWindowToken() : null;
            if (imm != null && focus != null) {
                if (show) {
                    imm.showSoftInput(focus, InputMethodManager.SHOW_IMPLICIT);
                } else if (token != null) {
                    imm.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }
        return thisp();
    }

    public void showDialogWithHtmlTextView(final Activity context, @StringRes int resTitleId, String text, boolean isHtml,
                                           DialogInterface.OnDismissListener dismissedListener) {
        ScrollView scroll = new ScrollView(context);
        AppCompatTextView textView = new AppCompatTextView(context);
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());

        scroll.setPadding(padding, 0, padding, 0);
        scroll.addView(textView);
        scroll.setScrollbarFadingEnabled(false);
        textView.setMovementMethod(new LinkMovementMethod());
        textView.setText(isHtml ? new SpannableString(Html.fromHtml(text)) : text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setPositiveButton(android.R.string.ok, null).setOnDismissListener(dismissedListener)
                .setView(scroll);
        if (resTitleId != 0) {
            dialog.setTitle(resTitleId);
        }
        dialogFullWidth(dialog.show(), true, false);
    }

    /**
     * Show dialog in full width / show keyboard
     *
     * @param dialog Get via dialog.show()
     */
    public void dialogFullWidth(AlertDialog dialog, boolean fullWidth, boolean showKeyboard) {
        try {
            Window w;
            if (dialog != null && (w = dialog.getWindow()) != null) {
                if (fullWidth) {
                    w.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                }
                if (showKeyboard) {
                    w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void blinkView(final View view) {
        if (view == null) {
            return;
        }

        final ObjectAnimator animator = ObjectAnimator
                .ofFloat(view, View.ALPHA, 0.2f, 1.0f)
                .setDuration(500L);

        view.setTag(BLINK_ANIMATOR_TAG, new WeakReference<>(animator));

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setAlpha(1.0f);
                view.setTag(BLINK_ANIMATOR_TAG, null);
            }
        });

        animator.start();
    }



}
