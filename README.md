<p align="center">
  <img src=".github/icon.png" width="180" alt="ARMSX3PKGISOLauncher">
</p>

<h1 align="center">ARMSX3PKGISOLauncher</h1>

<p align="center">
  Launcher for ARMSX3 games from Beacon
</p>
# PS3 PKG Launcher

## What is it for?

This app allows **Beacon Launcher** to launch PS3 games installed as **PKG/RAP** through **ARMSX3**.

It also supports normal PS3 ISO files.

## Setup

### 1. Select ARMSX3 in Beacon

First, configure **ARMSX3** as the PS3 emulator in Beacon.

Make sure your PS3 games are detected and launch correctly.

### 2. Create the "fake ISOs"

For games installed through PKG/RAP, create a small text file containing the game's **Title ID**.

You can find the Title ID in **ARMSX3** by **pressing and holding the game**. ARMSX3 will display the game's game code.

For example, create:

```text
RAIN.txt
```

and put the game's Title ID inside:

```text
NPEA00394
```

Then rename:

```text
RAIN.txt
```

to:

```text
RAIN.iso
```

Place the resulting file in:

```text
/storage/emulated/0/roms/ps3/
```

### Why do we rename it to `.iso`?

The file is **not a real ISO**. It is simply a small text file containing the game's Title ID.

We use the `.iso` extension because **ARMSX3 only accepts PS3 games in ISO format or folder format**. Using the `.iso` extension allows Beacon to recognize the game as a PS3 title while our launcher reads the Title ID and launches the installed PKG/RAP game through ARMSX3.

Repeat this process for each installed PKG/RAP game you want to add.

### 3. Change Beacon to PS3 PKG Launcher

Once all your fake ISOs have been created, go back to the **PS3 platform settings** in Beacon.

Change the selected application from **ARMSX3** to **PS3 PKG Launcher**.

Enable **Custom Launch** and enter:

```text
am start -n com.n30ryu.ps3pkglauncher/com.n30ryu.ps3pkglauncher.MainActivity -e file_path {file_path}
```

Save the configuration.

That's it.

## How it works

The launcher checks the size of the selected `.iso` file:

```text
Beacon
   ↓
PS3 PKG Launcher
   ↓
Is the ISO smaller than 100 KB?
   │
   ├── YES → Read it as a text file
   │           ↓
   │        Extract the Title ID
   │           ↓
   │        Launch the installed game through ARMSX3
   │
   └── NO → Treat it as a real ISO
               ↓
            Open it directly with ARMSX3
```

In other words:

* **Under 100 KB** → the file is treated as a fake ISO containing the game's Title ID.
* **Over 100 KB** → the file is treated as a real PS3 ISO and opened directly with ARMSX3.
