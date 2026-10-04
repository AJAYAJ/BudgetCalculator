# Backup and restore

- [x] Inspect data storage, authentication, existing sync, and settings.
- [x] Confirm direct Google Drive backup/restore with user.
- [x] Implement account-scoped snapshots, Drive authorization, Backup & restore UI, and first-use prompt.
- [x] Build debug app and instrumentation tests; run unit tests (10 passed).
- [x] Document Google Cloud configuration, limitations, and reinstall testing in `GOOGLE_DRIVE_BACKUP.md`.
- [ ] Configure Google Cloud OAuth/Drive API (developer action required).
- [ ] Run connected tests and live Drive/reinstall flow (no connected device available).

## Delivered scope
- Manual Back up now; no scheduled backups.
- Private Drive app-data folder using Google Identity Services authorization.
- Expenses, categories/subcategories, monthly budgets, loan people and transactions.
- Same Firebase app account and same Google Drive account required after reinstall.
- Download validation and confirmation before transactionally replacing that user's local data.
- Older Drive copies retained; only latest offered for restore, no retention management UI.

## Verification
- Gradle sync, `:app:assembleDebug`, `:app:testDebugUnitTest`, and `:app:assembleDebugAndroidTest` succeeded.
- `:app:connectedDebugAndroidTest` was blocked: no connected devices.
- New screen/ViewModel inspections reported style warnings only.
- Live Drive operations and UI/reinstall behavior remain unverified until setup and device testing.
