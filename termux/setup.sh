#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

REPO="hexxrr/TV-EXTREME"
RAW_APK="https://raw.githubusercontent.com/${REPO}/apk/app-debug.apk"
BASE="${HOME}/.tv-extreme"
mkdir -p "$BASE"

echo "=== TV EXTREME automatic TV deployment setup ==="
echo "Phone and TV must be on the same Wi-Fi."
pkg update -y
pkg install -y android-tools curl coreutils

read -r -p "TV IP address (example 192.168.1.50): " TV_IP
read -r -p "TV Wireless debugging PAIRING port: " PAIR_PORT
read -r -p "TV pairing code: " PAIR_CODE
read -r -p "TV Wireless debugging CONNECT port: " CONNECT_PORT

echo
echo "Pairing with TV..."
adb pair "${TV_IP}:${PAIR_PORT}" "$PAIR_CODE"
echo "Connecting to TV..."
adb connect "${TV_IP}:${CONNECT_PORT}"

adb devices
if ! adb -s "${TV_IP}:${CONNECT_PORT}" get-state >/dev/null 2>&1; then
  echo "ADB connection not ready. Check the TV IP/ports and accept the debugging prompt on TV."
  exit 1
fi

cat > "$BASE/config" <<EOF
TV_SERIAL="${TV_IP}:${CONNECT_PORT}"
APK_URL="${RAW_APK}"
EOF

echo
curl -fsSL "https://raw.githubusercontent.com/hexxrr/TV-EXTREME/main/termux/watch.sh" -o "$BASE/watch.sh"
echo "Setup complete. Starting automatic deployment watcher..."
echo "Keep Termux running. Press Ctrl+C to stop."
bash "$BASE/watch.sh"
