# Rights:

Forked from https://www.github.com/gsantner/notepad2

All licenses in the original repo above apply to this repo.

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

# High priority:
[H] - Use a single process (for editor and file manager)
[H] - Touch the parent folder whenever a note is edited (created issue https://github.com/gsantner/markor/issues/2382)

# Navigation:
- highlight briefly when going back (see Merge requests on github)
- total notes count (+ on folders?)

# Editing:
- Create custom syntax highlighting (make it like kate) see ./Notepad2-markor/app/src/main/java/net/gsantner/opoc/format/GsSimpleMarkdownParser.java
- remove auto insertion of tabs when enter is pressed on a line with multiple spaces
- prevent auto insertion of '-' when enter is pressed on a line beginning with -

# Appearance:
- darker scrollbars (make ones in files list always visible, this was previously attempted in 2bfc4d86d3a06eb0390e6b9e6daa20e723cbc079)
- Make hinted text have lighter grey colour at new note creation
- Move new note button to bottom right corner

# Updates:
- Pull the improvements from latest version
