# Centroid Tech Support Tools
This is a suite of tools to help aid in providing tech support for Centroid.<br><br>
__Directory Manager__<br>
'Activate' and 'Deactivate' CNC12 installations.<br>
On startup or Refresh the C: drive will be checked for any directories with names containing: cncm, cnct, cncr, cncp, or cncl. Information from these directories will then be pulled into the spreadsheet style interface. Here you can see if the directory is Active, the machine type, software version, date and time last modified, the path, and the first line from machine_notes.txt. By default the list is sorted by Active, Machine, and then Version but can be sorted differently by clicking the column headers.<br>
The table can be filtered by Board and Machine type using the buttons along the left side. Clicking the Filter button will turn on or off all filters at once.<br>
Left or right click a row in the table to select that row.<br>
Double left click a row to Activate the directory and start the executable.<br>
Right click for a context menu to select from several different functions for interacting with the selected installation:<br>
Refresh: Will force a refresh of the installation list.<br>
Activate: Will 'Activate' the selected installation meaning that the directory will be renamed properly to cncm, cnct, etc. This, if necessary, will also deactivate other directories.<br>
Deactivate: Will 'Deactivate' the selected installation renaming the directory. Example: cncm_5.40.04_acorn_01-26-26_10.40.23<br>
Open Directory: Opens a file explorer to the path of the selected installation.<br>
Open Notes: Opens the machine_notes.txt of the selected installation. If machine_notes.txt does not exist it is created.<br>
Open Message Log: Opens the msg_log.txt of the selected installation.<br>
Start Executable: Starts CNC12 from the selected installation. Note that this will launch the executable from the installation whether it's active or not.<br>
Deactivate All: Will deactivate all active installations.<br>

<img width="1920" height="1032" alt="Image" src="https://github.com/user-attachments/assets/4fb4f2a1-0eab-4443-89d8-bf6704d632e8" /><br>

__Install Tweaker__<br>
Tweak certain settings of installations by selecting from various different options.<br>
Development of this feature hasn't started yet.
