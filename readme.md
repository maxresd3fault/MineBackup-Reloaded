
# MineBackup Reloaded

**Info**: This is a fork of MineBackup 0.4.4 by ThisIsAreku. Credit goes to him for making the original plugin. My fork adds new features and fixes some bugs. Here is a list of notable changes from the original plugin:
* Fixed a bug where some server commands get executed numerous times. This happened because the original plugin author created multiple new CommandSenders instead of using the one Bukkit makes by default.
* Fixed a bug where plugin commands could not be run from the server console
* Changed the /mbck command to /minebackup and added additional arguments
* Added the option to *not* make backups if no players are connected as a toggle in the config. This is useful for server owners who do not need/want backups when no players have been online to edit the world.
* Added the option to make a backup when the last player online disconnects as a toggle in the config. This is useful in case the server were to die and corrupt the world after everyone logged off and the previous option (to not make backups of an empty server) was enabled.
* Added the option to keep X number of backups in favor of the previous system to keep X day's worth of backups. This is more streamlined and provides greater control over how many backups the server admin wants to keep.
* Added the option to skip automatically deleting any backups.

**Note:** This code was originally decompiled and then *heavily* modified by me, but I still need to do some refactoring the clean up the decompiled code.

