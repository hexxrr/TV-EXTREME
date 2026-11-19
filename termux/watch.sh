#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
BASE="${HOME}/.tv-extreme"
source "$BASE/config"
APK="$BASE/app-debug.apk"
HASH_FILE="$BASE/installed.sha256"

while true; do
  echo "[$(date '+%F %T')] Checking latest TV EXTREME build..."
  if curl -fLsS --retry 2 --connect-timeout 15 "$APK_URL" -o "$APK.tmp"; then
    mv "$APK.tmp" "$APK"
    NEW_HASH="$(sha256sum "$APK" | awk '{print $1}')"
    OLD_HASH="$(cat "$HASH_FILE" 2>/dev/null || true)"

    if [ "$NEW_HASH" != "$OLD_HASH" ]; then
      if adb -s "$TV_SERIAL" get-state >/dev/null 2>&1; then
        echo "New build found. Installing on TV..."
        if adb -s "$TV_SERIAL" install -r "$APK"; then
          printf '%s' "$NEW_HASH" > "$HASH_FILE"
          echo "TV EXTREME installed successfully."
        else
          echo "Install failed. Check TV storage and allow installation/debugging prompts."
        fi
      else
        echo "TV not connected. Reconnecting..."
        adb connect "$TV_SERIAL" >/dev/null || true
      fi
    else
      echo "Already up to date."
    fi
  else
    echo "APK not published yet. Waiting for the next successful GitHub build..."
  fi
  sleep 60
done
