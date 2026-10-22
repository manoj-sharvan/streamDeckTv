# MASTER PROMPT: M&D ANDROID TV — PERFORMANCE, OPTIMIZATION & UX IMPROVEMENT

## ROLE

Act as a Senior Android TV Engineer, Performance Optimization Specialist, UI/UX Designer, Accessibility Engineer, and QA Automation Engineer.

Your task is to audit, optimize, test, and improve my existing **M&D Android TV launcher application**. Do not rebuild the application from scratch. Work with the existing codebase, architecture, and implemented features.

The target device is an SPPL 2K Android TV running Android 14.

The application must deliver a smooth, modern, premium TV experience inspired by Apple TV, Google TV, and other high-quality television interfaces while maintaining its own M&D branding.

**Primary goals:**
1. Improve application performance and responsiveness.
2. Reduce unnecessary CPU, memory, and network usage.
3. Make navigation intuitive using a TV remote.
4. Fix incomplete or broken existing functionality.
5. Improve visual consistency and accessibility.
6. Ensure the application remains stable during long-running TV usage.
7. Validate every change on the actual Android TV whenever possible.

---

## PHASE 1 — PROJECT AUDIT AND BASELINE

Before changing any code:

1. Inspect the entire project structure and identify:
   - Framework, language, build system, and architecture.
   - Main launcher screen and sidebar/navigation components.
   - Live TV screen, channel lists, categories, and playback components.
   - Screensaver implementation and image-loading logic.
   - Google Photos integration, if implemented.
   - Settings, focus management, and remote-control handlers.
   - Background services, timers, event listeners, and network requests.
   - Image caching, state management, and persistence.
2. Identify unfinished features, duplicate code, performance bottlenecks, and potential crashes.
3. Run the existing build and tests before making changes.
4. Record baseline performance where measurement tools are available:
   - Cold and warm startup time.
   - Time to render the home screen.
   - Remote-control response latency.
   - Channel-list rendering time.
   - Memory usage and CPU consumption.
   - Frame drops and playback stability.
5. Save the current Git state or create a separate backup branch before modifications.

Create a prioritized audit report with:
- Issue.
- Evidence or reproduction steps.
- User impact.
- Severity.
- Proposed fix.
- Verification method.

Do not claim a performance improvement without measurements.

## PHASE 2 — PERFORMANCE OPTIMIZATION

### A. Application startup

- Minimize unnecessary work during application startup.
- Defer nonessential network requests and expensive initialization.
- Avoid blocking the main/UI thread.
- Load only the data required for the initial screen.
- Prevent repeated initialization when returning to the home screen.
- Preserve the current startup and launcher behavior.

### B. UI rendering

- Reduce unnecessary component or view re-renders.
- Avoid expensive calculations during rendering.
- Optimize state updates and event propagation.
- Use lazy loading or pagination for large channel and content lists.
- Recycle or reuse list items where supported by the current framework.
- Maintain stable item identifiers and focus state.
- Avoid unnecessary animations and expensive visual effects.
- Keep transitions smooth on the actual TV hardware.

### C. Memory and resource management

- Identify memory leaks and retained references.
- Release unused images, players, subscriptions, and resources.
- Cancel obsolete network requests and background jobs.
- Remove duplicate timers, listeners, and observers.
- Prevent image loading from consuming excessive memory.
- Handle repeated navigation between screens without increasing memory indefinitely.

### D. Image optimization

- Load images at appropriate display resolutions.
- Use caching with sensible size limits and eviction policies.
- Avoid loading every image at once.
- Handle unavailable, corrupted, or slow-loading images gracefully.
- Show placeholders while images load.
- Prevent repeated downloads when cached content is available.
- Preserve portrait and landscape image behavior.

### E. Network optimization

- Avoid duplicate requests and unnecessary polling.
- Add appropriate timeouts, retry limits, and cancellation.
- Cache suitable non-sensitive data.
- Show loading, empty, offline, and error states.
- Never block navigation because an optional online service is unavailable.
- Do not expose API keys, access tokens, or private credentials in source code or logs.

### F. Live TV optimization

- Optimize M3U playlist loading and parsing.
- Avoid repeatedly parsing the entire playlist during navigation.
- Cache parsed channel data where appropriate.
- Keep channel browsing responsive while data loads.
- Preserve language-based and channel-name-based categories.
- Optimize channel search and filtering.
- Prevent duplicate channels and invalid playlist entries where possible.
- Handle unavailable streams, buffering, and playback errors.
- Avoid automatically restarting playback during unrelated UI state updates.
- Preserve playback position and channel selection when appropriate.

Do not change IPTV stream URLs or remove existing supported playlist sources without a clear reason.

## PHASE 3 — TV REMOTE AND NAVIGATION EXPERIENCE

Make the entire interface usable with a standard Android TV remote, without requiring a mouse or touchscreen.

### Required behavior

- Every interactive element must be reachable by directional navigation.
- D-pad Up, Down, Left, and Right must move focus predictably.
- OK/Select must activate the focused item.
- Back must return to the previous logical screen or close the current overlay.
- Repeated Back presses must not unexpectedly exit the application.
- Long lists must scroll when focus approaches their boundaries.
- The focused item must remain visible.
- Focus must never disappear behind overlays or off-screen elements.
- Returning to a screen should restore its previous focus when appropriate.
- Focus indicators must be clearly visible from a normal TV viewing distance.
- Disable accidental double activation and repeated actions where necessary.

### Sidebar and top navigation

- Keep the sidebar compact and visually clean.
- Display icons by default if that is the current design.
- Reveal labels on focus or selection where appropriate.
- Remove redundant controls and confusing rearrangement indicators.
- Ensure the main content area responds correctly when the sidebar expands or collapses.
- Verify that every section is accessible using the remote.

### Scrolling

- Implement vertical and horizontal scrolling wherever content requires it.
- Ensure the scrollbar or another suitable scroll-position indicator is visible when useful.
- Prevent focus navigation from becoming trapped inside a list.
- Verify scrolling on long channel lists and large content collections.

## PHASE 4 — HOME SCREEN AND VISUAL UX

Create a clean, premium, consistent M&D TV interface.

- Use a consistent spacing system, typography, icon size, and visual hierarchy.
- Maintain readable text at a typical television viewing distance.
- Prevent clipped labels, overlapping content, and off-screen controls.
- Use subtle transitions rather than unnecessary animations.
- Keep important content accessible without excessive scrolling.
- Display meaningful loading, empty, and error states.
- Maintain contrast between focused, selected, and unselected items.
- Avoid excessive gradients, shadows, and background effects that reduce performance.
- Verify layouts at the supported TV resolutions and aspect ratios.

Do not redesign the entire interface without first evaluating the existing design. Improve it incrementally and preserve established user expectations.

## PHASE 5 — SCREENSAVER AND BACKGROUND BEHAVIOR

Preserve and verify the existing M&D screensaver requirements:

- Start the screensaver after five minutes of user inactivity.
- Display the inactivity countdown or timer statically in the bottom-left corner, if that feature is already part of the intended design.
- Use a ten-second transition or countdown where the existing specification requires it.
- Reset the inactivity timer after valid user interaction.
- Exit the screensaver immediately on the appropriate remote-control input.
- Prevent duplicate timers and multiple screensaver instances.
- Pause or resume relevant background tasks correctly.
- Handle app backgrounding, returning to the home screen, and device sleep appropriately.

### Google Photos

If Google Photos integration already exists, diagnose and improve it rather than pretending it is working.

- Verify the authentication method and supported API access.
- Check permissions, token expiry, image retrieval, and caching.
- Handle revoked access and expired credentials safely.
- Provide a clear fallback when Google Photos is unavailable.
- Do not scrape private account pages or bypass authentication.
- Do not assume direct access to a user's private Google Photos library is supported without verifying the actual integration.

If the current integration is not technically supported, explain the limitation and recommend a supported alternative before implementing a replacement.

## PHASE 6 — RELIABILITY AND ERROR HANDLING

- Prevent crashes caused by missing data, malformed playlists, or failed requests.
- Add defensive validation at appropriate boundaries.
- Ensure loading indicators cannot remain stuck indefinitely.
- Handle empty collections and first-run scenarios.
- Make optional features fail gracefully without breaking the home screen.
- Prevent multiple simultaneous playback or screensaver instances.
- Ensure state remains consistent after navigation and app recreation.
- Keep diagnostic logs useful without exposing private information.

Do not hide errors by silently swallowing exceptions. Log actionable diagnostic details without leaking sensitive data.

## PHASE 7 — CODE QUALITY AND MAINTAINABILITY

- Follow the project's existing architectural conventions.
- Extract reusable components only when doing so improves clarity or maintainability.
- Remove dead code only after confirming it is unused.
- Avoid unnecessary dependencies and large framework migrations.
- Avoid introducing multiple competing state-management systems.
- Use clear naming, predictable state transitions, and appropriate error handling.
- Add comments for non-obvious TV focus and lifecycle behavior.
- Keep configuration and secrets outside committed source code.
- Update documentation when behavior or setup requirements change.

Do not perform broad refactors unrelated to measurable problems.

## PHASE 8 — TESTING AND VALIDATION

Test each change independently.

### Functional tests

- Application launches successfully.
- Sidebar navigation works.
- Main-screen content scrolls correctly.
- Live TV categories and channel lists work.
- Channel selection and playback remain stable.
- Playlist URL changes work.
- Back navigation behaves correctly.
- Screensaver starts after the configured inactivity period.
- Screensaver exits on user interaction.
- Image loading and caching behave correctly.
- Existing settings and saved preferences remain intact.

### Performance tests

Measure before and after wherever practical:

- Startup time.
- Screen-rendering time.
- Remote-control response time.
- Memory usage.
- CPU usage.
- Frame drops.
- Playlist parsing time.
- Image-loading time.
- Playback and buffering stability.

### Stress tests

- Navigate continuously between screens.
- Scroll through large playlists.
- Switch channels repeatedly.
- Disconnect and reconnect the network.
- Open and close the screensaver repeatedly.
- Leave the application running for an extended period.
- Test with missing images and malformed playlist entries.

### Build verification

- Run the appropriate project build.
- Run available automated tests and static checks.
- Resolve regressions introduced by the changes.
- Confirm that the APK installs and launches on the target Android TV.
- Review relevant logs for crashes, focus issues, and resource leaks.

Never report a test as passed unless it was actually executed.

## PHASE 9 — IMPLEMENTATION STRATEGY

Work in small, independently verifiable stages:

1. Audit and establish a baseline.
2. Fix critical crashes and broken core navigation.
3. Optimize startup and rendering.
4. Improve D-pad focus and scrolling.
5. Optimize live TV and playlist handling.
6. Optimize image loading and caching.
7. Fix screensaver lifecycle and background behavior.
8. Improve visual consistency and accessibility.
9. Run regression, stress, and performance tests.
10. Produce a final report.

After each stage:
- Explain the issue found.
- Make the smallest safe code change.
- Run the relevant tests.
- Review the resulting diff.
- Document remaining issues.
- Proceed only when the stage is verified or its limitations are documented.

Do not attempt to modify everything in one massive, unverified change.

## PHASE 10 — FINAL REPORT

At completion, provide:

1. Summary of changes.
2. Files modified and why.
3. Bugs fixed.
4. Performance measurements before and after.
5. Tests actually executed and their results.
6. Features that remain incomplete.
7. Known limitations specific to the target TV.
8. Installation and validation instructions.
9. Recommended next improvements ranked by impact and effort.

Classify improvements as:
- P0 — crashes, data loss, or critical navigation failures.
- P1 — major performance or core usability issues.
- P2 — secondary UX and reliability improvements.
- P3 — optional polish and future enhancements.

## STRICT RULES

- Do not rebuild the application from scratch.
- Do not delete working features to simplify implementation.
- Do not disable Android system packages, the stock launcher, or essential services.
- Do not change device settings or install an APK without explicit authorization.
- Do not expose credentials or private user data.
- Do not invent test results or performance improvements.
- Do not replace a working integration with a mock implementation.
- Do not make broad changes without a backup and a clear rollback path.
- Preserve the existing M&D TV identity and all working user-facing functionality.

## START NOW

Begin with Phase 1 only.

Inspect the existing project, run the current build and available tests, and establish a performance baseline. Return the prioritized audit report and recommended implementation sequence before making broad changes.

After the audit, implement the highest-impact, lowest-risk improvements first and verify each change before proceeding.
