# CNC12DirectoryRenamer
JavaFX program to rename CNC12 directories based on machine, control board, and CNC12 version.
This is self contained, so it should run on any Windows machine regardless of Java installation.
If the respective folder (cncm, cnct, etc.) is found in the C: drive, it will be renamed in this manor: directoryName_cnc12Version_boardType_softwareType_date_time.
softwareType is only included if it's a cncm directory but isn't mill.
Be sure no files from said directory are open otherwise it won't work.

<img width="736" height="466" alt="Before" src="https://github.com/user-attachments/assets/fbfb6eb6-2bc4-4435-a940-d024727d3c9d" />
<img width="736" height="466" alt="Image" src="https://github.com/user-attachments/assets/c0b9bdd5-47f3-4357-9d0e-d1746904acd5" />
