# FEATURE: GLOBAL LOADER FOR M&D ANDROID TV

Act as a senior Android TV developer. Implement a reusable, lightweight global loading system in the existing M&D TV launcher without breaking current features.

## 1. Global Loading UI

Create a reusable loading component that can be used across the entire application.

- Use a modern, minimal M&D TV-themed animated spinner or subtle loading indicator.
- Display a short message when useful, such as "Loading apps...", "Loading Live TV...", or "Loading settings...".
- Keep the loader centered in the active content area or use a full-screen overlay when a blocking operation genuinely requires it.
- Use smooth fade-in and fade-out transitions.
- Maintain proper contrast and visibility on the TV screen.
- Ensure the animation remains lightweight and does not cause unnecessary CPU or GPU usage.
- Prevent multiple loaders from appearing simultaneously.

## 2. Show Loaders for Top-Level Apps and Sections

Integrate the loader into the following operations where applicable:

- Opening the Apps section.
- Loading the installed-app list.
- Launching an external Android application, where the launcher can detect and control the transition.
- Opening Live TV and initializing the channel list.
- Loading IPTV playlists or refreshing channel data.
- Opening Settings or other major sections that require asynchronous initialization.
- Loading Google Photos or screensaver images.
- Refreshing data or performing other substantial asynchronous operations.

Do not show a loader for simple local screen transitions that complete immediately.

## 3. Correct Loading Lifecycle

For each operation:

1. Start loading when the actual asynchronous operation begins.
2. Display the loader only if the operation takes longer than approximately 200–300 milliseconds.
3. Hide the loader when the operation finishes successfully, fails, or is cancelled.
4. Display an appropriate error, empty state, or retry option when needed.
5. Prevent stale requests from hiding the loader for a newer operation.
6. Prevent duplicate requests and overlapping loading states where appropriate.
7. Add a safe timeout for operations that could otherwise remain loading indefinitely.
8. Ensure the loading state is cleaned up when the user navigates away.

Never leave a loader running indefinitely.

## 4. Android App Launching Behavior

Handle Android application launching correctly.

- Show immediate visual feedback when the user selects an app.
- Launch the selected app using the existing supported Android mechanism.
- Do not block the main thread while waiting for an external app.
- Do not assume the launcher can observe the external app's internal loading progress.
- Where reliable lifecycle callbacks are available, hide or reset the launcher loader when the app opens or the user returns.
- If the external app cannot be launched, show a useful error and restore the normal launcher UI.
- Do not display a permanent spinner while an external app is running.

Important: distinguish between loading the app list inside M&D TV and launching a separate Android application. These are different operations and require different lifecycle handling.

## 5. Navigation and Remote-Control Support

- Keep the selected item and D-pad focus stable during loading.
- Prevent accidental repeated activation while a blocking operation is in progress.
- Allow Back to cancel or leave a loading screen when safe.
- Restore focus to the previous item when returning from an external app.
- Do not let a loader intercept remote-control input unnecessarily.
- Ensure navigation remains responsive while background data loads.

## 6. Performance Requirements

- Use one shared loading component and consistent loading-state management.
- Avoid unnecessary state updates and re-renders.
- Do not reload installed apps or playlists every time the user changes focus.
- Cache suitable data and refresh it only when necessary.
- Keep loading animations efficient on the target SPPL Android 14 TV.
- Avoid showing a full-screen loader when only a small section needs refreshing.
- Do not introduce additional dependencies unless they are necessary.

## 7. Testing and Acceptance Criteria

Verify the following:

- Apps section opens without unnecessary delay.
- Installed apps are displayed correctly after loading.
- Slow operations show a loader only when appropriate.
- Fast operations do not flash a distracting spinner.
- Failed operations stop loading and show a recoverable error.
- Repeated selections do not create duplicate loaders.
- Back navigation and D-pad focus work correctly.
- External app launching and returning do not leave stale loading overlays.
- Live TV, playlist refresh, and screensaver loading continue to work.
- No existing functionality is removed or replaced by mock behavior.

## IMPLEMENTATION INSTRUCTION

First inspect the existing architecture and identify how each top-level section loads data. Reuse the existing state-management and navigation approach. Implement the global loader incrementally, test each integration, and report which operations are genuinely covered.

Do not simply add a decorative spinner. Connect every loader to the real loading lifecycle of the operation it represents.
