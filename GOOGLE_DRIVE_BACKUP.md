# Google Drive backup and restore

## What is implemented

Open **More → Backup & restore**:

- **Back up now** uploads a versioned JSON snapshot to Google Drive's private `appDataFolder`.
- **Check latest backup** queries Drive and displays its server-reported modification time.
- **Restore from Google Drive** downloads the latest snapshot for the signed-in Firebase user, validates it, shows a confirmation, and replaces only that user's local data in one Room transaction.
- A first-use prompt offers the restore screen after sign-in. Its marker is stored in `noBackupFilesDir`, so a new installation can show it even if Android restores other app files.

Included: categories, subcategories, expenses (integer minor units), monthly budgets, loan people, and loan transactions.
Excluded: passwords, Firebase sessions/tokens, Google access tokens, app preferences, and unrelated accounts' records.

## Required Google Cloud setup

Use the Google Cloud project associated with this app's Firebase project.

1. In **APIs & Services → Library**, enable **Google Drive API**.
2. Configure **Google Auth Platform / OAuth consent screen** branding, support email, audience, and applicable privacy policy information.
3. Configure the scope `https://www.googleapis.com/auth/drive.appdata`. The app does not request access to all Drive files.
4. If the OAuth app is in testing, add the Google accounts you will test with as test users.
5. Create an **Android OAuth client** for package `com.vegam.budgetcalculator` and your signing certificate's SHA-1. Run `./gradlew :app:signingReport` to obtain the debug fingerprint. Add clients for the release and Google Play app-signing certificates as applicable.
6. Keep the same Cloud/OAuth project and package identity across releases so the app retains access to its app-data folder.
7. Test on a device/emulator with Google Play services and a Google account.

This client-only flow uses Google Identity Services `AuthorizationClient`; it does not need a web client secret, a backend refresh token, or changes to Firebase email/password authentication. Do not embed OAuth secrets in the APK. The Firebase login account and the Google Drive account are separate: restore needs the same Firebase UID **and** the same Google Drive account that created the backup. Creating a new Firebase account with the same email after deleting the original does not recover its UID.

## User flow after reinstall

1. Before uninstalling, tap **Back up now**, choose/authorize the Google account if requested, and wait for the success message.
2. Reinstall and sign in to the **existing app account**.
3. Choose **Open backup & restore** from the prompt, or open it from More.
4. Tap **Restore from Google Drive**, using the same Google account.
5. Review the backup date and record counts, then tap **Replace and restore**.

Restore is intentionally not silent: it requires Drive authorization where necessary and explicit confirmation before replacing existing records. Do not upload an empty installation before restoring; the newest upload becomes the restore candidate.

## Safety and limitations

- Backups are **manual**, not scheduled. Android Auto Backup remains separate and its timing is not a guarantee that current records have been saved.
- Backups live in Drive's hidden app-data folder, not as visible files in My Drive. They depend on the user retaining that Google account and its app data.
- Each upload creates a fresh file. Older copies are retained to avoid overwriting the last good backup during an interrupted upload. This version offers only the latest snapshot; there is no backup history selection or retention cleanup UI. Copies consume Drive storage.
- A snapshot is limited to **20 MiB**. Upload completion is acknowledged by Drive before success is shown. Requests have timeouts; errors can be retried manually.
- Google access tokens are used transiently, not written into backups or app preferences. A 401 clears the cached token so a subsequent attempt can reauthorize.
- JSON is sent over HTTPS and stored under Google's account/access controls. This feature does **not** add password-based or end-to-end file encryption. Include backup handling in the app's privacy policy.
- Validation checks format/version, account ownership, duplicate IDs, expense category links, loan links/enums, and duplicate budget periods before restore. Database insertion errors roll back the entire replacement. Conflicting IDs from another local account abort rather than overwrite that account.
- Restore replaces this account's local records; newer local edits will be lost after confirmation. It does not merge snapshots, sync across devices, or reconcile the separate legacy expense-only Firestore upload path.
- Deleting a Google backup, revoking access, using the wrong Google account, changing Cloud projects, or losing the original app account can prevent restore.

## Verification

Completed:
- Gradle sync.
- `:app:assembleDebug`.
- `:app:testDebugUnitTest`: 10 passing tests, including 9 new backup validation/serialization tests.
- `:app:assembleDebugAndroidTest`: instrumentation tests compile.

Not completed:
- `:app:connectedDebugAndroidTest` could not run because no device was connected. New tests cover account-isolated repeatable restore and rollback on cross-account ID collision.
- Live Drive authorization, upload/download, UI interactions, and uninstall/reinstall have not been tested; they require Cloud setup and a connected device.

Before release, run the connected tests and a full reinstall test with representative data. Also test cancelled consent, no network, wrong Google/app account, no backup, token revocation, invalid/unsupported snapshots, and cancelling the replacement confirmation. Confirm that Android's own automatic restore does not mask a missing Drive restore during the test.
