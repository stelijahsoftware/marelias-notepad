# Rights:

Forked from https://www.github.com/gsantner/markor

Apache 2.0 license (see [LICENSE.txt](./LICENSE.txt); all licenses in the original repo above apply to this repo).

<img src="./icon/icon.png" alt="icon" width="25%" />

A txt notes editor which is a simplified and heavily trimmed down and improved light version of [Markor](https://www.github.com/gsantner/markor).

### Forked started from 25-May-2024, last commit included was a2afb69bc6edcf9ee35d33060a02ae7cc0292847. Then cherry picked commits until f31cfa73. If you would like to pull new commits, start after f31cfa73

# TODO:
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

# Naming:
[h] - do not use hour and minute for automatic numbering, instead use sequential numbers (if name of new note exists do not open existing one, rather number sequentially)
[m] - rename to mar-elias notes (see commit "Rename app to notepad2" and "Rename folder from markor to notepad2" (Jul17))
- Don't use currentTimeMillis(), instead copy the stamp from the just written file https://github.com/gsantner/markor/pull/2422#issuecomment-2365364977

[h] - Add an about screen that shows the original developer (+ a link to his github) + license name + my own link

### Copyright © 2017-2024
**Gregor Santner**
\n**Official project sources:**
[Project page](https://github.com/gsantner/notepad2#readme) | [Source code](https://github.com/gsantner/notepad2) | [F-Droid](https://f-droid.org/repository/browse/?fdid=net.gsantner.notepad2)
LICENSE: APACHE 2.0 (+ LINK)


# Organisation:
- highlight briefly when going back (see Merge requests on github)
[h] - show total notes count (+ on folders?)
[m] - publish to gplay

# Appearance:
- darker scrollbars + (make scrollbar in files list always visible, this was previously attempted in commit title "Attempt to change scrollbar color..")
- make save button bigger (disable auto-save?)
- Make hinted text have lighter grey colour at new note creation

## Commands:
./gradlew build --warning-mode all

./gradlew clean
./gradlew build

## Deploy apk (release):
To edit the app version, edit build.gradle inside app/
`Build -> Generate signed bundle/APKs -> APK -> create new key -> choose 'DefaultRelease'`
then find the apk here:
`notepad2-markor/app/flavorDefault/release/net.elyahw.notepad2-v11-0.8-flavorDefault-release.apk`
