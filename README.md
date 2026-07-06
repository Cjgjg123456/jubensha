# JubenSha Game - Startup Guide

## Quick Start

### Method 1: Double-click (Recommended)
Double-click **`Quick Start.bat`** - This will start the game immediately!

### Method 2: Interactive Mode
Double-click **`start.bat`** - Choose between:
- [1] Local Mode
- [2] Public Mode  
- [3] Show addresses

---

## Access Addresses

### Local Access
- **Web**: http://localhost:8080
- **Game**: localhost:8888

### Public Access
- **Web**: http://5ff7cc2.r11.cpolar.top
- **Game Server**: tcp://11.tcp.cpolar.top:14220
- **Cpolar Dashboard**: http://localhost:9200

---

## Scripts Available

| File | Description |
|------|-------------|
| `Quick Start.bat` | One-click start (recommended) |
| `start.bat` | Interactive menu with options |
| `restart-cpolar.bat` | Restart Cpolar service |

---

## Features

✅ **Auto-detect Java environment**
✅ **Auto-compile if needed**
✅ **Local & Public modes**
✅ **Public address display**
✅ **Cpolar integration**

---

## Troubleshooting

### Issue: Script closes immediately
**Solution**: 
- Use `Quick Start.bat` instead
- Or run from command prompt:
  ```bash
  cd "c:\Users\lenovo\Desktop\Java联机\jubensha - 11"
  .\start.bat
  ```

### Issue: Java not found
**Solution**: Install Java 17 or higher from https://java.com

### Issue: JAR file not found
**Solution**: The script will auto-compile. If it fails, run:
  ```bash
  mvn clean package -DskipTests
  ```

---

## Files in Project

```
jubensha - 11/
├── Quick Start.bat      <- Double-click this!
├── start.bat            <- Interactive mode
├── start.ps1           <- PowerShell version
├── restart-cpolar.bat   <- Restart Cpolar
├── src/                <- Source code
├── target/             <- Compiled files
└── pom.xml             <- Maven config
```

---

## Need Help?

1. **Check Java**: Run `java -version` in CMD
2. **Check Cpolar**: Visit http://localhost:9200
3. **Check Ports**: Ensure 8080 and 8888 are available

---

**Last Updated**: 2026-05-26
