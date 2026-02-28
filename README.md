
<img width="150" src="https://raw.githubusercontent.com/dragoonDorise/pegasus-companion/refs/heads/main/logo.png" alt="Pegasus Companion">

# Pegasus Companion

Android app that downloads themes and Artwork for [Pegasus Frontend](https://pegasus-frontend.org/). box art, wheels, and screenshots come from the ScreenScraper API.

## Features

- **ROM scanning**: Detects game systems by folder name, lists ROMs per system
- **Theme downloader**: Pick your favorite Pegasus Theme
- **Artwork downloading**: Queries ScreenScraper API, downloads images (box art, wheel, screenshot)
- **Pegasus-compatible**: Saves artwork in the folder structure Pegasus Frontend expects
- **Skip logic**: Re-runs skip ROMs that already have all artwork
- **Background downloads**: Foreground service with notification progress

## ROM Directory Structure

Organize ROMs in system subfolders:
```
ROMs/
├── nes/
│   ├── Contra (U).zip
│   └── Super Mario Bros (U).zip
├── snes/
│   └── Chrono Trigger (U).sfc
└── gba/
    └── Pokemon Emerald (U).gba
```

Artwork is saved as:
```
ROMs/nes/media/Contra (U)/boxFront.png
ROMs/nes/media/Contra (U)/wheel.png
ROMs/nes/media/Contra (U)/screenshot.png
```