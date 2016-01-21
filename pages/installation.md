---
layout: page-fullwidth
header:
    image_fullwidth: 'meshHeader-scaled2-lighter.png'
title: "Installation Instructions"
teaser: ""
permalink: /installation/
---
The installation of HELYX-OS has been made easier for the user by leveraging a command line installer.  Below we will go through the steps to setup HELYX-OS in just a few minutes.

## Provided Platforms

Platform | Availability
-------------|----------------
Linux 32 Bit | none
Linux 64 Bit | [free](https://github.com/ENGYS/HELYX-OS/releases)
Mac OSX      | none
64 Bit Windows | [with paid support only](http://engys.com/products/helyx-os)

## Installation of the Latest 64 Bit Linux Binaries

1.  Download the latest 64 Bit binary from the [project front page](http://engys.github.io/HELYX-OS/) or from the list of [releases](https://github.com/ENGYS/HELYX-OS/releases).  The installer will have the form:

        <date>-Engys-HELYX-OS-<version>--linux-x86_64-FULL.bin

2.  Once the binary is downloaded, open a Linux terminal and navigate to the location of the newly downloaded HELYX-OS installer.

3.  Change the permissions of the installer by executing ```chmod 755 <installer name>``` in the terminal, similar to:

        :~$ chmod 755 20160114-Engys-HELYX-OS-2.3.1--linux-x86_64-FULL.bin

4.  Start the installer by typing ```./<installer name>``` while in the terminal, similar to:

        :~$ ./20160114-Engys-HELYX-OS-2.3.1--linux-x86_64-FULL.bin

5.  At this point, the installer will begin and the user will be guided through the installation process:

        Waiting for HELYX-OS installer to start...
    
        
        This program will install HELYX-OS on your system. Do you want to continue?
        1) Yes
        2) No
        #?
    
    In order to continue with the installation, you must type **1** in the terminal and press enter.<br>

6.  The user will be presented with a copy of the Gnu Public License (press enter or space to read through the GPL):

        Do you accept the terms of agreement?
        1) Agree
        2) Exit
        #?

    In order to continue with the installation, you must agree by typing **1** in the terminal and press enter.<br>

7.  Select which components to install:

        Select components to install
        1) HELYX-OS-GUI
        2) HELYX-OS-GUI and Kernel
        #?

    For users with exisitng installations of OpenFOAM version 2.4.0, select to install only the GUI by typing **1** and hitting enter.  To install the HELYX-OS GUI and the compatable version of OpenFOAM (Ubuntu 14.04 or later only), type **2** and press enter. 

8.  The user will be prompted to enter **the full path** to where HELYX-OS will be intalled:

        Select destination folder for HELYX-OS GUI:

    For example, if a user named Scott wants to install HELYX-OS to their home folder (this is the suggested location), then they woudl enter:

        Select destination folder for HELYX-OS GUI: /home/scott/
    
    and a folder named ```Engys```, containing HELYX-OS will be created in the user's home folder.  The GUI will install and produce the following output:

        Select destination folder for HELYX-OS GUI: /home/scott
        Installing in: /home/scott
        Installing HELYX-OS GUI ................done.

9.  If you previously selected option **2** in step 7, you will now be prompted to select your linux distribution:
    
        Please select your linux distribution:
        1) Ubuntu (14.04 or later)
        2) Other
        #?
    
    Selecting **1** will install the compatable OpenFOAM deb package for 64 bit Ubuntu on 14.04 or later.  For other linux distributions, the compatable version of OpenFOAM must be installed manually by the user.

10.  To start HELYX-OS in the terminal, execute the ```HELYX-OS.sh``` file located in ```engys/HELYX-OS/v2.3.1``` directory similar to:
    
        :~$ ~/Engys/HELYX-OS/v2.3.1/HELYX-OS.sh```<br>

## Compillation from Source
For the latest instructions on how to compile HELYX-OS from source, please see the git repository [README](https://github.com/ENGYS/HELYX-OS/blob/master/README.md) file inside the [HELYX-OS repository](https://github.com/ENGYS/HELYX-OS).