# TV EXTREME

Android TV utility app with a GitHub Actions debug build and optional Termux-to-TV deployment.

## Automatic TV deployment (one-time phone setup)

Requirements:
- OnePlus/Android phone and Android TV on the same Wi-Fi.
- Termux installed (use the current Termux build from F-Droid or the official Termux GitHub releases).
- TV has **Developer options > Wireless debugging** enabled.

Steps:
1. On the TV open **Developer options > Wireless debugging > Pair device with pairing code**.
2. In Termux run:
   ```sh
   curl -fsSL https://raw.githubusercontent.com/hexxrr/TV-EXTREME/main/termux/setup.sh -o setup.sh
   bash setup.sh
   ```
3. Enter the TV IP, the pairing port, pairing code, and then the separate connect port shown by Wireless debugging. Accept the debugging authorization prompt on TV.
4. Leave the watcher running in Termux. It checks the public APK feed every 60 seconds and installs a new build when its SHA-256 changes.

The GitHub Actions workflow publishes the APK to the `apk` branch after a successful build. The phone is the bridge to the home Wi-Fi; GitHub-hosted runners cannot directly reach a TV on a private home network.

Notes:
- Pairing is a one-time manual security approval. Android intentionally does not allow a cloud workflow to silently pair with or control a TV on a private LAN.
- Keep Termux running and exempt it from battery optimization for continuous background checks. If Android kills Termux, reopen it and run `bash ~/.tv-extreme/watch.sh`.
- This installs the app with `adb install -r`; it does not bypass Android install permissions or TV security prompts.
