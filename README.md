# Rights:

Forked from https://www.github.com/gsantner/markor

Apache 2.0 license (see [LICENSE.txt](./LICENSE.txt); all licenses in the original repo above apply to this repo).

<img src="./icon/icon.png" alt="icon" width="25%" />

A txt notes editor which is a simplified and heavily trimmed down and improved light version of [Markor](https://www.github.com/gsantner/markor).

### Forked starting from (if you would like to pull new commits, start from this):

https://github.com/gsantner/markor/commit/d24c66e93a1a6162c796c8a47acf0621012660c8

# TODO:
X - Remove all occurrences of:
X - audio
X - epub
X - ascii
X - csv
X - See todos from Snotepad.
X - Fix: search when launched from inside a file (8cc1d0def12211bcc1886e95471bf86895171d35 breaks file search)
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
X - remove auto insertion of tabs when enter is pressed on a line with multiple spaces + prevent auto insertion of '-' when enter is pressed on a line beginning with '-'. Solved in bd240b3df2fb81a80ff3ea402c03693c918a10d9
X - remove local file settings - done in 1a1c71047ef7a7a8babd002a2390b7ab8c5f88f9
X - main screen: remove import from device option; replace '...' on top right with cog icon
X - Create custom syntax highlighting (make it like kate) see ./Notepad2-markor/app/src/main/java/net/gsantner/opoc/format/GsSimpleMarkdownParser.java - done in 0a36d0300b795aea4d027836e94c48c0a9e3b5a7
X - Move new note button to bottom right corner
X - Clean up settings menu
X - add to highlighting: [h] [m] [l]
X - Disable highlighting inside other highlights (see commits ending in 80f4109e76fbc566e23e661c6eace18158893054)

# Naming:
- do not use hour and minute for automatic numbering, instead use sequential numbers (if name of new note exists do not open existing one, rather number sequentially)
- rename to mar-elias notes (see 5a3cd97b05e1efb958bf0885463323de17e6a585 and fadfbde3fa08cc4001d7e7ddc17e8ccf79bc938d)

# Organisation:
[blocked] - Use a single process (for editor and file manager). It has to do with starting and stopping activities (see SettingsActivity, MainActivity.. etc)
[h] - Pull any improvements from latest version (single process implemented here https://github.com/gsantner/markor/commit/c5fe529515830dd16ba5dea6e14eadd016b1a1bf); start pulling from current state (see reference in project information; update it when pulling new changes)
- highlight briefly when going back (see Merge requests on github)
- show total notes count (+ on folders?)
- convert local file settings to global + set defaults

# Appearance:
- darker scrollbars + (make scrollbar in files list always visible, this was previously attempted in 2bfc4d86d3a06eb0390e6b9e6daa20e723cbc079)
- Make hinted text have lighter grey colour at new note creation
- make save button bigger (disable auto-save?)
