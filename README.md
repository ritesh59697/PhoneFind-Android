# PhoneFind - Android Anti-Theft & Device Management Client

PhoneFind is a native Android security client built with Kotlin and Jetpack Compose. It provides real-time anti-theft monitoring, SIM card change detection, periodic location pings, and remote device control (Alarm, Lock, Location Ping, and Wipe).

![PhoneFind Architecture](https://raw.githubusercontent.com/ritesh59697/PhoneFind/main/public/logo.png)

## Features

- 🔐 **Anti-Tamper Device Admin**: Requires security PIN verification to deactivate device admin protection.
- 📡 **Periodic Location Pings**: Background WorkManager job reporting GPS location & battery status every 5 minutes.
- 📱 **SIM Swap Detection**: Background worker checking SIM serial SHA-256 hash every 15 minutes.
- 🔔 **Remote Alarm Sound**: High-volume alarm playback overriding system silent/vibrate settings.
- ⚡ **Automated Web Sync**: Continuous synchronization with Next.js web management dashboard.

## Tech Stack

- **UI Framework**: Jetpack Compose + Material3
- **Network**: Retrofit 2 + OkHttp + Gson
- **Security**: EncryptedSharedPreferences (AndroidX Security)
- **Background Work**: WorkManager + Play Services Location
- **Push Notifications**: Firebase Cloud Messaging (FCM)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
