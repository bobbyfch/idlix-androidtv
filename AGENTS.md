# Android TV repository workflow

- `main` is the only routine working branch. Do not create Codex, AI, feature, preview or temporary branches, pull requests or extra worktrees unless the owner explicitly asks.
- This is an **Android TV application**, not a website: GitHub stores source and optional APK releases. Do not introduce Cloudflare Workers/Pages or GitHub Pages just to build an Android application.
- Use Gradle's existing Android build and tests for relevant changes; do not add duplicate web deployment or CI runners.
