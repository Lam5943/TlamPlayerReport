# TLamPlayerReport

**Advanced Player and Bug Reporting System for Minecraft PaperMC/Folia**

[![License](https://img.shields.io/badge/License-Custom-blue.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.16.x--1.21.x-green.svg)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net)

> **Author:** TranLam (Midnight)  
> **Version:** 1.0.0

---

## 📋 Table of Contents

- [Features Overview](#-features-overview)
- [Installation Guide](#-installation-guide)
- [Compilation Guide](#-compilation-guide)
- [Configuration Guide](#️-configuration-guide)
- [Commands & Permissions](#-commands--permissions)
- [Google Sheets Setup](#-google-sheets-setup)
- [Database Setup](#️-database-setup)
- [Key System](#-key-system)
- [Multi-Language Support](#-multi-language-support)
- [Troubleshooting](#-troubleshooting)
- [Support](#-support)

---

## ✨ Features Overview

### Core Features

- **📝 Report System**
  - Player reporting with customizable categories
  - Bug reporting system
  - Intuitive GUI-based report submission
  - Configurable 2-hour cooldown between reports
  - Anti-spam and duplicate detection

- **👮 Admin Management**
  - Comprehensive admin interface for viewing reports
  - Filter and sort reports by status, type, and date
  - Delete individual or bulk reports
  - Real-time notifications for new reports
  - Detailed report viewing with all metadata

- **💾 Flexible Data Storage**
  - **MySQL** support for large-scale servers
  - **SQLite** support (default) for easy setup
  - Automatic database schema management
  - Auto-cleanup of old reports

- **📊 Google Sheets Integration**
  - Automatic report logging to Google Sheets
  - Real-time synchronization
  - Configurable spreadsheet columns
  - Easy external analysis and reporting

- **🔔 Discord Integration**
  - Webhook support for report notifications
  - Rich embeds with report details
  - Configurable notification triggers
  - Role/user mentions support

- **🌍 Multi-Language Support**
  - English (en)
  - Vietnamese (vi)
  - Spanish/Español (es)
  - Fully customizable language files
  - Easy to add new languages

- **🔐 License Key System**
  - Secure key validation
  - Online and offline verification modes
  - Protection against unauthorized use

- **⚙️ Platform Compatibility**
  - PaperMC 1.16.x - 1.21.x
  - Folia scheduler compatibility
  - Async-safe operations
  - Paper API integration

---

## 📦 Installation Guide

### Prerequisites

- **Minecraft Server:** PaperMC or Folia (1.16.x - 1.21.x)
- **Java:** Version 21 or higher
- **RAM:** Minimum 512MB allocated (recommended 1GB+)

### Step-by-Step Installation

1. **Download the Plugin**
   - Download the latest `TLamPlayerReport-1.0.0.jar` from releases
   - Or compile from source (see [Compilation Guide](#-compilation-guide))

2. **Install the Plugin**
   ```bash
   # Copy the JAR file to your server's plugins directory
   cp TLamPlayerReport-1.0.0.jar /path/to/server/plugins/
   ```

3. **Start Your Server**
   - Start or restart your Minecraft server
   - The plugin will generate default configuration files

4. **Configure License Key**
   - Open `plugins/TLamPlayerReport/config.yml`
   - Enter your license key:
     ```yaml
     license:
       key: "YOUR-LICENSE-KEY-HERE"
     ```

5. **Customize Settings** (Optional)
   - Edit `config.yml` for main settings
   - Edit `gui.yml` for GUI customization
   - Edit language files in `languages/` folder

6. **Reload the Plugin**
   ```
   /tlamplayerreportadmin reload
   ```

---

## 🔨 Compilation Guide

### Prerequisites

- **JDK 21** or higher ([Adoptium Temurin](https://adoptium.net) recommended)
- **Gradle** (included via wrapper)
- **Git** (for cloning)

### Building from Source

1. **Clone the Repository**
   ```bash
   git clone https://github.com/Lam5943/TlamPlayerReport.git
   cd TlamPlayerReport
   ```

2. **Build with Gradle**
   ```bash
   # On Linux/Mac
   ./gradlew clean build
   
   # On Windows
   gradlew.bat clean build
   ```

3. **Locate the Output**
   - The compiled JAR will be in `build/libs/`
   - Look for `TLamPlayerReport-1.0.0.jar`

4. **Installation**
   ```bash
   cp build/libs/TLamPlayerReport-1.0.0.jar /path/to/server/plugins/
   ```

### Build Troubleshooting

- **Error:** "JAVA_HOME not set"
  - Set JAVA_HOME to your JDK 21 installation path

- **Error:** "Could not resolve dependencies"
  - Check your internet connection
  - Try: `./gradlew build --refresh-dependencies`

- **Error:** "Unsupported class file version"
  - Ensure you're using JDK 21 or higher

---

## ⚙️ Configuration Guide

### config.yml

The main configuration file for TLamPlayerReport.

#### License Settings
```yaml
license:
  key: "ENTER-YOUR-LICENSE-KEY-HERE"
  verification-url: "https://api.example.com/verify"
  online-verification: false  # Set to true for online validation
```

#### General Settings
```yaml
settings:
  language: en                    # Language: en, vi, or es
  custom-command: report          # Custom alias for /report
  notify-admins: true            # Notify admins of new reports
  close-gui-on-report: true      # Close GUI after submission
  cooldown-seconds: 7200         # Cooldown in seconds (2 hours)
  max-reports-per-player: 10     # Max pending reports per player
```

#### Report Limits
```yaml
settings:
  report-limits:
    player-reports-per-day: 5    # Max player reports per day
    bug-reports-per-day: 3       # Max bug reports per day
```

#### Anti-Spam Protection
```yaml
settings:
  anti-spam:
    enabled: true                        # Enable anti-spam
    duplicate-check: true                # Prevent duplicate reports
    duplicate-time-window: 3600          # Window in seconds (1 hour)
```

#### Database Configuration

**SQLite (Default - Recommended for Small/Medium Servers)**
```yaml
database:
  type: SQLITE
  sqlite:
    file-name: reports.db
```

**MySQL (Recommended for Large Servers)**
```yaml
database:
  type: MYSQL
  mysql:
    host: localhost
    port: 3306
    database: tlamplayerreport
    username: root
    password: your_password
    pool-size: 10
    connection-timeout: 30000
    max-lifetime: 1800000
```

#### Auto-Cleanup
```yaml
database:
  auto-cleanup:
    enabled: true              # Enable automatic cleanup
    days: 30                   # Delete reports older than X days
    run-interval: 86400        # Run every X seconds (24 hours)
```

#### Report Categories
```yaml
categories:
  player:
    enabled:
      - cheating              # Hacking/cheating
      - harassment            # Toxic behavior
      - spam                  # Chat spam
      - inappropriate-name    # Offensive username
      - griefing              # Destroying builds
      - bug-abuse             # Exploiting bugs
      - other                 # Other violations
    require-evidence: false   # Require proof
  
  bug:
    enabled:
      - gameplay              # Game mechanics
      - visual                # Graphics/display
      - performance           # Lag/TPS
      - command               # Command issues
      - other                 # Other bugs
    require-description: true # Require details
```

#### GUI Settings
```yaml
gui:
  update-interval: 20          # Update every X ticks (20 = 1 second)
  close-on-complete: true      # Close GUI after report
  
  sounds:
    enabled: true
    open: BLOCK_CHEST_OPEN
    click: UI_BUTTON_CLICK
    success: ENTITY_PLAYER_LEVELUP
    error: ENTITY_VILLAGER_NO
```

### gui.yml (INVENTORY_GUI.yml)

Customize the GUI appearance and layout.

#### Main Menu
```yaml
main-menu:
  title: "&6&lReport Menu"
  size: 27                      # Must be multiple of 9 (9-54)
  items:
    player-report:
      slot: 11
      material: PLAYER_HEAD
      name: "&c&lReport Player"
      lore:
        - "&7Report a player for breaking rules"
        - ""
        - "&eClick to continue"
```

#### Category GUI
```yaml
categories:
  player:
    title: "&cReport Reason"
    size: 27
    categories:
      cheating:
        slot: 10
        material: BARRIER
        name: "&c&lCheating"
        lore:
          - "&7Player using hacks or cheats"
```

**Customization Tips:**
- Slot numbers: 0-53 for a 54-slot inventory
- Materials: Use Bukkit Material names
- Colors: Use `&` color codes (&a = green, &c = red, etc.)
- Lore: Array of strings for item descriptions

### Language Files

Located in `plugins/TLamPlayerReport/languages/`

#### File Structure
```
languages/
├── en/
│   └── messages.yml
├── vi/
│   └── messages.yml
└── es/
    └── messages.yml
```

#### Customizing Messages
```yaml
prefix: "&8[&6TLamReport&8]&r"

commands:
  no-permission: "{prefix} &cYou don't have permission."
  cooldown: "{prefix} &cPlease wait {time} seconds."

report:
  success: "{prefix} &aReport submitted!"
```

**Placeholders:**
- `{prefix}` - Message prefix
- `{player}` - Player name
- `{time}` - Time remaining
- `{id}` - Report ID
- `{type}` - Report type
- `{count}` - Count value

---

## 📜 Commands & Permissions

### User Commands

| Command | Description | Permission | Aliases |
|---------|-------------|------------|---------|
| `/report` | Open report GUI | `tlamplayerreport.report` | `/reportplayer`, `/reportbug` |

### Admin Commands

| Command | Description | Permission | Aliases |
|---------|-------------|------------|---------|
| `/tlpr help` | Show help menu | `tlamplayerreport.admin` | `/tlpradmin`, `/tlamplayerreport`, `/reportadmin` |
| `/tlpr list [page]` | List all reports | `tlamplayerreport.admin.view` | - |
| `/tlpr view <id>` | View report details | `tlamplayerreport.admin.view` | - |
| `/tlpr delete <id>` | Delete a report | `tlamplayerreport.admin.delete` | - |
| `/tlpr clear` | Clear all reports | `tlamplayerreport.admin.clear` | - |
| `/tlpr reload` | Reload configuration | `tlamplayerreport.admin.reload` | - |

### Permission Nodes

#### User Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `tlamplayerreport.report` | Use report command | `true` |
| `tlamplayerreport.view.own` | View own reports | `true` |

#### Admin Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `tlamplayerreport.admin` | All admin features | `op` |
| `tlamplayerreport.admin.view` | View all reports | `op` |
| `tlamplayerreport.admin.delete` | Delete reports | `op` |
| `tlamplayerreport.admin.clear` | Clear all reports | `op` |
| `tlamplayerreport.admin.reload` | Reload plugin configuration | `op` |
| `tlamplayerreport.notify` | Receive report notifications | `op` |

#### Bypass Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `tlamplayerreport.bypass.cooldown` | Bypass report cooldown | `op` |
| `tlamplayerreport.bypass.limit` | Bypass report limits | `op` |

---

## 📊 Google Sheets Setup

### Prerequisites

1. Google account
2. Google Cloud Platform project
3. Google Sheets API enabled

### Step 1: Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Click "Create Project"
3. Name it (e.g., "TLamPlayerReport")
4. Click "Create"

### Step 2: Enable Google Sheets API

1. In your project, go to "APIs & Services" > "Library"
2. Search for "Google Sheets API"
3. Click "Enable"

### Step 3: Create Service Account

1. Go to "APIs & Services" > "Credentials"
2. Click "Create Credentials" > "Service Account"
3. Fill in details:
   - Name: `tlamplayerreport-service`
   - ID: (auto-generated)
4. Click "Create and Continue"
5. Grant role: "Editor"
6. Click "Done"

### Step 4: Generate Credentials

1. Click on your service account
2. Go to "Keys" tab
3. Click "Add Key" > "Create new key"
4. Choose "JSON"
5. Download the file
6. Rename it to `credentials.json`
7. Place it in `plugins/TLamPlayerReport/` folder

### Step 5: Create Spreadsheet

1. Go to [Google Sheets](https://sheets.google.com)
2. Create a new spreadsheet
3. Name it (e.g., "Player Reports")
4. Copy the Spreadsheet ID from URL:
   ```
   https://docs.google.com/spreadsheets/d/SPREADSHEET_ID_HERE/edit
   ```

### Step 6: Share Spreadsheet

1. Click "Share" button
2. Add service account email (found in `credentials.json`, field: `client_email`)
3. Give "Editor" permission
4. Click "Send"

### Step 7: Configure Plugin

Edit `config.yml`:
```yaml
google-sheets:
  enabled: true
  credentials-file: credentials.json
  spreadsheet-id: YOUR_SPREADSHEET_ID_HERE
  log-on-submit: true
  log-on-update: false
```

### Step 8: Restart Plugin

```
/tlpr reload
```

### Spreadsheet Columns

The plugin will automatically create these columns:
- Report ID
- Report Type
- Reporter
- Reported Player (for player reports)
- Category
- Description
- Status
- Timestamp
- Admin Notes

---

## 🗄️ Database Setup

### SQLite Setup (Default)

**No setup required!** SQLite is file-based and works out of the box.

**Configuration:**
```yaml
database:
  type: SQLITE
  sqlite:
    file-name: reports.db  # Created automatically
```

**Location:** `plugins/TLamPlayerReport/reports.db`

**Backup:**
```bash
cp plugins/TLamPlayerReport/reports.db plugins/TLamPlayerReport/reports.db.backup
```

---

### MySQL Setup

#### Step 1: Install MySQL

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install mysql-server
```

**CentOS/RHEL:**
```bash
sudo yum install mysql-server
```

#### Step 2: Secure MySQL

```bash
sudo mysql_secure_installation
```

#### Step 3: Create Database

```bash
mysql -u root -p
```

```sql
CREATE DATABASE tlamplayerreport CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'tlpr_user'@'localhost' IDENTIFIED BY 'secure_password_here';
GRANT ALL PRIVILEGES ON tlamplayerreport.* TO 'tlpr_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### Step 4: Configure Plugin

Edit `config.yml`:
```yaml
database:
  type: MYSQL
  mysql:
    host: localhost
    port: 3306
    database: tlamplayerreport
    username: tlpr_user
    password: secure_password_here
    pool-size: 10
    connection-timeout: 30000
    max-lifetime: 1800000
```

#### Step 5: Restart Plugin

```
/tlpr reload
```

#### Database Schema

Tables are created automatically:
- `reports` - Main reports table
- `report_meta` - Additional report metadata

---

## 🔐 Key System

### Obtaining a License Key

1. Contact the author: TranLam (Midnight)
2. Purchase or request a license key
3. Receive your unique key

### Configuring Your Key

Edit `config.yml`:
```yaml
license:
  key: "YOUR-LICENSE-KEY-HERE"
  online-verification: false  # Set true for online validation
  verification-url: "https://api.example.com/verify"
```

### Verification Modes

#### Offline Verification (Default)
- Key validated locally
- No internet required
- Faster startup

#### Online Verification
- Key validated with remote server
- Prevents key sharing
- Requires internet connection

### Troubleshooting Keys

**"Invalid license key"**
- Check for typos in config.yml
- Ensure no extra spaces or quotes
- Contact support if purchased

**"Cannot connect to verification server"**
- Check internet connection
- Verify verification-url is correct
- Try offline mode temporarily

---

## 🌍 Multi-Language Support

### Available Languages

- 🇬🇧 **English** (`en`) - Default
- 🇻🇳 **Vietnamese** (`vi`) - Tiếng Việt
- 🇪🇸 **Spanish** (`es`) - Español

### Changing Language

Edit `config.yml`:
```yaml
settings:
  language: vi  # Change to: en, vi, or es
```

Then reload:
```
/tlpr reload
```

### Adding a New Language

1. **Copy English Template**
   ```bash
   cp -r plugins/TLamPlayerReport/languages/en plugins/TLamPlayerReport/languages/de
   ```

2. **Translate Messages**
   Edit `plugins/TLamPlayerReport/languages/de/messages.yml`

3. **Update Config**
   ```yaml
   settings:
     language: de
   ```

4. **Reload Plugin**
   ```
   /tlpr reload
   ```

### Translation Guidelines

- Keep placeholders like `{player}`, `{time}` unchanged
- Maintain color codes (`&a`, `&c`, etc.)
- Test all messages in-game
- Respect character limits for GUI items

---

## 🔧 Troubleshooting

### Common Issues

#### Plugin Not Loading

**Symptom:** Plugin doesn't appear in `/plugins` list

**Solutions:**
1. Check Java version: `java -version` (must be 21+)
2. Check console for errors
3. Verify JAR file is in `plugins/` folder
4. Check server version (must be Paper 1.16.x - 1.21.x)

#### Database Connection Failed

**Symptom:** "Failed to initialize database"

**Solutions:**

For MySQL:
```bash
# Test connection
mysql -h localhost -u tlpr_user -p tlamplayerreport

# Check if MySQL is running
sudo systemctl status mysql

# Verify credentials in config.yml
```

For SQLite:
```bash
# Check file permissions
ls -la plugins/TLamPlayerReport/reports.db

# Ensure plugin folder is writable
chmod 755 plugins/TLamPlayerReport/
```

#### Google Sheets Not Working

**Symptom:** Reports not appearing in sheets

**Solutions:**
1. Verify `credentials.json` exists in plugin folder
2. Check Spreadsheet ID is correct
3. Ensure service account has editor access
4. Check console for Google API errors
5. Verify internet connectivity

#### Commands Not Working

**Symptom:** "Unknown command"

**Solutions:**
1. Check plugin is loaded: `/plugins`
2. Verify command spelling: `/tlpr` not `/tlrp`
3. Check permissions: `/lp user <name> permission check tlamplayerreport.admin`
4. Try reload: `/tlpr reload`

#### GUI Not Opening

**Symptom:** Report GUI doesn't open

**Solutions:**
1. Check console for errors
2. Verify `gui.yml` syntax (use YAML validator)
3. Check inventory size (must be 9, 18, 27, 36, 45, or 54)
4. Test with default config
5. Check for plugin conflicts

#### Cooldown Not Working

**Symptom:** Players can spam reports

**Solutions:**
1. Check config: `cooldown-seconds` is set
2. Verify player doesn't have bypass permission
3. Check database is connected
4. Restart plugin

#### Language Not Changing

**Symptom:** Still shows English after changing language

**Solutions:**
1. Verify language file exists: `languages/vi/messages.yml`
2. Check config.yml: `language: vi`
3. Reload plugin: `/tlpr reload`
4. Check for YAML syntax errors in language file

### Performance Issues

#### High Memory Usage

**Solutions:**
1. Reduce database pool size
2. Lower GUI update interval
3. Enable auto-cleanup for old reports
4. Consider upgrading to MySQL

#### TPS Drops

**Solutions:**
1. Set database operations to async
2. Increase auto-cleanup interval
3. Optimize MySQL queries
4. Check for other plugin conflicts

### Getting Help

#### Before Asking for Help

1. Check console for error messages
2. Try with default configuration
3. Test with other plugins disabled
4. Check server version compatibility

#### Where to Get Help

- **GitHub Issues:** [Report bugs](https://github.com/Lam5943/TlamPlayerReport/issues)
- **Discord:** Join our support server
- **Email:** Contact TranLam (Midnight)

#### Information to Include

When asking for help, provide:
- Server version and platform (Paper/Folia)
- Plugin version
- Java version
- Console errors (use pastebin.com)
- Config files (remove sensitive data)
- Steps to reproduce the issue

---

## 📞 Support

### Author

**TranLam (Midnight)**

### Links

- **GitHub:** [Lam5943/TlamPlayerReport](https://github.com/Lam5943/TlamPlayerReport)
- **Issues:** [Report Bugs](https://github.com/Lam5943/TlamPlayerReport/issues)
- **Website:** https://github.com/TranLam

### Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

### License

This project uses a custom license. See the key system for usage rights.

---

## 🙏 Acknowledgments

- PaperMC team for the excellent server software
- Folia team for the multi-threaded scheduler
- Google for Sheets API
- All contributors and testers

---

**Made with ❤️ by TranLam (Midnight)**

*Last Updated: January 2026*