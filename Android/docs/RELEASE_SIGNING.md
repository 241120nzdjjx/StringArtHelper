# Release signing

Official releases use a private Android keystore that is stored offline and is never committed to this repository or uploaded to GitHub Actions.

Gradle reads signing details only from environment variables:

```text
STRING_ART_KEYSTORE
STRING_ART_KEYSTORE_PASSWORD
STRING_ART_KEY_ALIAS
STRING_ART_KEY_PASSWORD
```

Example release build:

```bash
export STRING_ART_KEYSTORE=/absolute/path/to/official-release.jks
export STRING_ART_KEYSTORE_PASSWORD='...'
export STRING_ART_KEY_ALIAS='...'
export STRING_ART_KEY_PASSWORD='...'
gradle --no-daemon :app:assembleRelease
```

Before publishing:

1. Increase both `versionName` and `versionCode`.
2. Build from a clean source checkout.
3. Verify ZIP alignment and APK signatures with Android Build Tools.
4. Confirm that the signer SHA-256 is:

   ```text
   6A:06:8C:3D:40:95:25:1F:E7:1D:00:09:FC:06:51:30:C5:E6:62:BA:32:97:ED:A8:C3:8F:CE:94:CA:70:76:4F
   ```

5. Publish the APK and its SHA-256 digest together.
6. Keep at least two encrypted offline backups of the keystore and recovery information.

Losing the signing key prevents future versions from updating existing official installations. Exposing it allows an attacker to impersonate official updates.
