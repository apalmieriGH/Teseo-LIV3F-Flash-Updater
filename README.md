# Teseo-LIV3F-Flash-Updater
Java application to upgrade the firmware of the Teseo-LIV3F

* To select the device port click the dropdown menu on the left, select the correct port an then click open.

* To start the upgrade procedure with the default image file click the button "Update FW >>>" on the right.

* To select a different binary file click on Tools>Advanced... and then select the desired binary.
  The procedure should start automatically after your selection.

* To close the device port click on the close button on the left.

This application requires the X-NUCLEO-GNSS1A1 together with a Nucleo board or an Arduino Due board. 
Additionally the X_NUCLEO_GNSS1A1_VirtualCOMPort Arduino sketch should be loaded into the board.

The sketch can be found at the following link:
https://github.com/stm32duino/X-NUCLEO-GNSS1A1
