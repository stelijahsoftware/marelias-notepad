# Mar-Elias Notepad
A Simple .txt file editor with highlights (<a href="https://writing-tools.github.io/marelias-notepad/">website</a>)

<!-- [<img src="./docs/icon/Google_Play_Store_badge_EN.svg" alt="Download from Google Play" height="80">](https://play.google.com/store/apps/details?id=) -->

# Rights:

Forked from Gregor Santner's [Markor](https://www.github.com/gsantner/markor)

Apache 2.0 license (see [LICENSE.html](./docs/LICENSE.html); all licenses in the original repo (Markor) apply to this repo).

<img src="./docs/icon/icon.png" alt="icon" width="25%" />

A simple .txt notes editor, heavily trimmed down version of Markor, with customised highlights.

# Declaration:
I forked this project as I thought the original notepad was too bulky and difficult to use, it seemed to me that the authors provided the ability to do minor tweaks, even on individual txt files! A notepad shouldn't be like this. So I created a simpler, easier to use, trimmed down version of the original notepad and I removed 100s of flags and options that were standing in the way of the notepad to be a practical one. If one day the markor authors decide to adopt this simplicity, there would be no more need for me to continue maintaining this project, and I would stop doing so. This is how open source works, and this project is a healthy development of it.

# Contact:
feedbackATstelijahDOTcoDOTuk

### Forking note:
Forked started from 25-May-2024, last commit included was a2afb69bc6edcf9ee35d33060a02ae7cc0292847. Then cherry picked commits until f31cfa73. If you would like to pull new commits, start after f31cfa73

### TODO:
Bug: top system bar invisible on 4a

- colour: TODO:/Note:/Now:
- turn on dark mode
- button to insert dash at line begin?

[m] Keep recently searched for items in search list. keep history when searching for a file (same as history in search and replace inside file)
[m] File count to folders after folder name (information already available when selecting multiple files). Do not count folders as 1 instead as num of contents
[m] rename note after being opened by pressing on title (see branch)
[m] When press back on note empty, ask to delete. Auto-delete empty notes on save + disable auto-save?
[m] Remove save delay
[m] Make selecting text and scrolling up faster (selecting text from middle of screen until top)
[m] Button to insert "- " at the beginning of the current line

[L] - Navigation: highlight recently opened folder (not just files)
[L] - Navigation: Put top bar on bottom for easier access to save button & rename..etc?
[L] - Visibility: make scrollbars darker+bigger when pressed (see opencontacts app)
[L] - Naming: Do not use hour and minute for automatic numbering, instead use sequential numbers (if name exists, number sequentially rather than open existing note)
[L] - Naming/Sorting: add modifDate_creatDate?Eg`20250122-20241127 note.txt`.the first date gets updated on modification.Needs to make sure that no new note is created.
[L] - Share notes app
[L] - Sorting: Ability to pin notes to top?
[L] - Sorting: Add file with keywords to select sorting of specific folder, e.g.: "Sorting: 1/2/3", "Linenumbers: 1/0" (just like dolphin) + so save starred documents + pinned
[L] - Don't use currentTimeMillis(), instead copy the stamp from the just written file https://github.com/gsantner/markor/pull/2422#issuecomment-2365364977

### Ignored tasks:
X - Add button to insert date yyy-mm-dd?
X - Change dropdown text (of new file creation button) to dropdown menu (select title without date as name)
X - search and replace (inside a file): make it possible to replace all occurrences (already exists) + case insensitive

### Changelog from Markor:
X - Visibility: pointer not visible inside highlights
X - Visibility: invisible selection color within highlights when selecting text
X - update selection icon
X - Made searching case insensitive
X - Made icons dark in top bar
X - Made top navigation bars light
X - Remove rename item from menu
X - Remove confirmation when deleting a note
X - Add swipe to rename (not: Rename note title from pressing `note title` on document edit screen/button to rename note.)
X - Remove cancel button when selecting
X - Fix theme when system dark mode is enabled (some window colours are correct).
X - show total notes count (including folders) on main screen
X - Add git integration info (add how to note in 'about' screen)
X - [m] 10. add % -- comments
X - [h] Make numbers smaller in numbered lines
X - make white bg of icon when starting up
X - [h] `Open with dialog` for odt,doc,docx,odtf,pdf (files not ending in .txt/md) (see implementation in original app).SEE DIALOG it shows you when you try to open an odt file
X - highlight recently opened file.
X - Remove unuseful stop after first match option
X - Make file browser scrollbar wider + draggable
X - Don't make document scrollbars darker, but auto disappear when paused for 2 secs
X - Improve rename dialog
X - Make document edit scrollbars wider
X - Make font color black
X - Added a button to paste text
X - when highlighting, move dedicated info button to expandable list (removed altogether)
X - [h] BIG: `Plus button` should not show dialog, instead auto add note (like snotepad)
X - Add plus button to create folders (Add separate button for folders)
X - Remove all occurrences of: audio, epub, ascii, csv
X - See todos from Snotepad.
X - Fix: search when launched from inside a file (breaks file search)
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
X - Create custom syntax highlighting (make it like kate) see ./app/src/main/java/org/gsantner/opoc/format/GsSimpleMarkdownParser.java
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
X - make default selected format bold

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
app/flavorDefault/release/flavorDefault-release.apk
```

# Possible colours:
<color name="primary_dark">#2f3355</color>
<color name="primary_light">#CED9DD</color>
<color name="accent_light">#FF9800</color>
<color name="accent">#F04B4B</color>
<color name="dark__folder">#72B3D9</color>
<color name="dark__folder">#0040ff</color>

# Template note:
# This is a green line

## This is a blue line

// Red line

This is a list:
- element 1
- element 2
- element 3
X - this is a finished task

All numbers will be coloured orange, for example 2024-09-28

Urls and websites will be highlighted: https://www.github.com/gsantner/markor

You can also add **bold** and _italic_ text

***** Asterisks will also be coloured

You can also add importance tags like:
[h] task 1
[m] task 2
[l] task 3
