# Mar-Elias Notepad
A Simple .txt file editor with highlights (<a href="https://stelijah.com/products/marelias-notepad.html">website</a>)

[<img src="https://stelijah.com/products/marelias-notepad/icon/Google_Play_Store_badge_EN.svg" alt="Download from Google Play" height="80">](https://play.google.com/store/apps/details?id=org.marelias.notepad&pli=1)

# Rights:

Forked from Gregor Santner's [Markor](https://www.github.com/gsantner/markor)

Apache 2.0 license (see [LICENSE.txt](./LICENSE.txt); all licenses in the original repo (Markor) apply to this repo).

<img src="https://stelijah.com/products/marelias-notepad/icon/icon.png" alt="icon" width="25%" />

A simple .txt notes editor, heavily trimmed down version of Markor, with customised highlights.

# Declaration:
I forked this project as I thought the original notepad was too bulky and difficult to use, it seemed to me that the authors provided the ability to do minor tweaks, even on individual txt files! A notepad shouldn't be like this. So I created a simpler, easier to use, trimmed down version of the original notepad and I removed 100s of flags and options that were standing in the way of the notepad to be a practical one. If one day the markor authors decide to adopt this simplicity, there would be no more need for me to continue maintaining this project, and I would stop doing so. This is how open source works, and this project is a healthy development of it.

# Contact:
feedbackATstelijahDOTcom

### Forking note:
Forked started from 25-May-2024, last commit included was a2afb69bc6edcf9ee35d33060a02ae7cc0292847. Then cherry picked commits until f31cfa73. If you would like to pull new commits, start after f31cfa73

### TODO:
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

### Changelog from Markor (modifications):
See [CHANGELOG.txt](./CHANGELOG.txt)

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
