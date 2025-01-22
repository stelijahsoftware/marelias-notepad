# Mar-Elias Notepad
A Simple noteapd with highlights.

[<img src="icon/Google_Play_Store_badge_EN.svg" alt="Download from Google Play" height="80">](https://play.google.com/store/apps/details?id=net.marelias.notepad)

# Rights:

Forked from Gregor Santner's [Markor](https://www.github.com/gsantner/markor)

Apache 2.0 license (see [LICENSE.txt](./LICENSE.txt); all licenses in the original repo (Markor) apply to this repo).

<img src="./icon/icon.png" alt="icon" width="25%" />

A simple text notes editor and heavily trimmed down version of [Markor](https://www.github.com/gsantner/markor), with customised highlights.

# Declaration:
I forked this project as I thought the original notepad was too bulky and difficult to use, it seemed to me that the authors provided the ability to do minor tweaks, even on individual txt files! A notepad shouldn't be like this. So I created a simpler, easier to use, trimmed down version of the original notepad and I removed 100s of flags and options that were standing in the way of the notepad to be a practical one. If one day the markor authors decide to adopt this simplicity, there would be no more need for me to continue maintaining this project, and I would stop doing so. This is how open source works, and this project is a healthy development of it.

# Contact:
feedbackATstelijahDOTcoDOTuk

### Forking note:
Forked started from 25-May-2024, last commit included was a2afb69bc6edcf9ee35d33060a02ae7cc0292847. Then cherry picked commits until f31cfa73. If you would like to pull new commits, start after f31cfa73

### TODO:
[H] - BIG: `Open with dialog` for odt, doc, docx, odtf and pdf (see implementation in original app).
    - Open files not ending in .txt/md in respective apps (feature already exists in original app, transfer it)
    - SEE THE DIALOG it shows you when you try to open an odt file.

[H] - BIG: `Plus button` should not show dialog, instead auto add note (like snotepad)
    - Rename title from pressing `note title` on opening.
    - Add plus button to create folders from top menu

[H] - color symbols
[h] - make default selected format bold
[H] - BIG: Add file with keywords to select sorting of specific folder, e.g.: "Sorting: 1/2/3", "Linenumbers: 1/0"
[H] - Auto-delete empty notes on save + disable auto-save?
[H] - Do not use hour and minute for automatic numbering, instead use sequential numbers (if name exists, number sequentially rather than open existing note)

[M] - Put top bar on bottom for easier access to save button & rename..etc
[M] - Make selecting text and scrolling up faster (selecting text from middle of screen until top)
[M] - Keep recently searched for items in search list

[L] - Show total notes count (+ on folders?) (information already available when selecting multiple files)
[L] - Don't use currentTimeMillis(), instead copy the stamp from the just written file https://github.com/gsantner/markor/pull/2422#issuecomment-2365364977
[L] - Highlight briefly when going back (see Merge requests on github) (this feature is not implemented yet in upstream)
[L] - Add git integration info (add how to note in 'about' screen)

### Ignored tasks:
X - Add button to insert date yyy-mm-dd?
X - Change dropdown text (of new file creation button) to dropdown menu (select title without date as name)

### Changelog from Markor:
X - Remove all occurrences of: audio, epub, ascii, csv
X - See todos from Snotepad.
X - Fix: search when launched from inside a file (breaks file search)
X - call them notes 1 & notes 2
X - Remove bottom bar in text editing
X - do not open multiple instances (single instance only), not one for each file and for each reopened file. I.e just like snotepad
X - Change the versioning to match the new versions
X - remove the bottom dark bar (Files)
X - change app name from markor to notepad2
X - one click starts new note with date and time
X - scrollbar always visible + clickable
X - simplify new file dialog
X - Change the default flags of (sort by date; reverse order.. etc)
X - Touch the parent folder whenever a note is edited (created issue https://github.com/gsantner/markor/issues/2382)
X - change icon
X - remove auto insertion of tabs when enter is pressed on a line with multiple spaces + prevent auto insertion of '-' when enter is pressed on a line beginning with '-'
X - remove local file settings
X - main screen: remove import from device option; replace '...' on top right with cog icon
X - Create custom syntax highlighting (make it like kate) see ./Notepad2-markor/app/src/main/java/net/gsantner/opoc/format/GsSimpleMarkdownParser.java
X - Move new note button to bottom right corner
X - Clean up settings menu
X - add to highlighting: [h] [m] [l]
X - Disable highlighting inside other highlights (see commits ending in)
X - convert local file settings to global + set defaults
X - Add version number to About
X - Use a single process (for editor and file manager). It has to do with starting and stopping activities (see SettingsActivity, MainActivity.. etc)
X - Pull any improvements from latest version (single process implemented here https://github.com/gsantner/markor/commit/c5fe529515830dd16ba5dea6e14eadd016b1a1bf); start pulling from current state (see reference in project information; update it when pulling new changes)
X - Add an about screen that shows the original developer (+ a link to his github) + license name + my own link
X - rename to mar-elias notes (see commit "Rename app to notepad2" and "Rename folder from markor to notepad2" (Jul17))
X - Add **bold** to and italics notes _template_ + x - cross over
X - disable red underlining of incorrectly typed words
X - remove hinted text on new note creation
X - Notes: Shift down the page
X - Automatically create default note template in storage location on install
X - When renaming a note, automatically highlight the time part of the name i.e. "_hhmmss" inside the full form "yyymmdd_hhmmss" for easier renames.
X - Make hinted text have lighter grey colour at new note creation (completely removed)
X - make scrollbar in files list always visible
X - Darker scrollbars + wider
X - green & red bg + [r + make 3 hashes orange ###
X - Colour: ✓ ✗
X - colour line beginning with $ as code
X - FIX: Touch all parent folders on save starting from home
X - FIX: Creating new folder with no name moves directory navigation up (goes up one level)
X - Publish to gplay

### Build Commands:
./gradlew build --warning-mode all

./gradlew clean
./gradlew build

### Deploy apk (release):
1. Increase app version by editing: (increase both `versionName` and `versionCode`)
```
app/build.gradle
```
2. Then tag in git:
```
git tag -ln && git tag -a v1.0.6 -m "v1.0.6" && git push origin --tags
```
3. Then generate apk: (choose Android app bundle if submitting to Google Play store)
```
Build -> Generate signed bundle/APKs -> APK -> create new key -> choose 'DefaultRelease'
```
4. Then find the apk here:
```
app/flavorDefault/release/net.elyahw.notepad2-v11-0.8-flavorDefault-release.apk
```

# Possible colours:
<color name="primary_dark">#2f3355</color>
<color name="primary_light">#CED9DD</color>
<color name="accent_light">#FF9800</color>
<color name="accent">#F04B4B</color>
<color name="dark__folder">#72B3D9</color>
<color name="dark__folder">#0040ff</color>
