# MASTER PROMPT: Premium Android TV UX Redesign, Glassmorphism, Channel Selection & Remote Navigation Fixes

## ROLE

Act as a Senior Android TV Developer, UI/UX Designer, Motion Design Engineer, and TV Remote Navigation Specialist.

Analyze my existing Android TV application and transform its user experience into a premium, polished, modern television interface with buttery-smooth navigation, glassmorphism styling, responsive animations, reliable channel selection, and complete physical remote support.

**Important:** Extend and improve the existing application. Do not rebuild it from scratch, remove existing features, or replace working functionality with mock implementations.

Inspect the source code, identify the root causes, implement the changes, build the application, and verify the results.

---

## PHASE 1: AUDIT THE EXISTING APPLICATION

Before changing any code:

1. Inspect the complete project structure and identify the framework and UI architecture.
2. Analyze the startup loading screen and existing application theme.
3. Inspect the Home screen, left sidebar, right-side main content, Live TV categories, channel cards, and channel-name labels.
4. Trace how remote-control key events, focus states, click handlers, and scroll containers work.
5. Identify why the sidebar works with the remote while the right-side content does not.
6. Identify why channel details and channel names appear but cannot be selected reliably.
7. Review existing transitions, animations, image loading, and rendering performance.
8. Check relevant application logs and build configuration.

Create a prioritized list of root causes, then fix them incrementally.

Preserve the existing application branding, IPTV playlist integration, channel data, and working features.

## PHASE 2: FIX CHANNEL NAME SELECTION AND CARD CLICKING

### Current problem

The Live TV screen displays channel information on the right side, including channel names and other details. However, I cannot reliably move to a channel name using the remote and click or select it.

This is a critical issue that must be fixed.

### Required functionality

- Make each intended channel card and its channel name selectable using the physical TV remote.
- Allow the user to move focus directly onto a channel card or channel-name area.
- Pressing OK/SELECT must activate the focused channel.
- The channel name must not be merely decorative text if it is intended to be selectable.
- Make the appropriate card or channel row the primary focus target.
- Where suitable, allow the channel name and card surface to activate the same channel action without triggering it twice.
- Prevent overlapping click listeners, duplicate playback requests, and conflicting parent-child click handlers.
- Make focus states clearly visible against the background.
- Ensure channel selection continues to work when the list is scrolled.
- Restore focus sensibly when returning from playback.
- Handle unavailable channels and playback errors gracefully.

### Channel card interaction design

Create a consistent interaction pattern:

1. Unfocused: clean, readable channel card.
2. Focused: smooth visual highlight, subtle scale or elevation, and a clear focus border.
3. Selected: a brief, tasteful confirmation animation.
4. Playing: show an appropriate playing or active-channel indicator when the playback state confirms it.

Use actual playback state to determine whether a channel is playing. Do not display a false playing indicator.

Ensure that channel names, logos, and other useful information remain legible on a television screen.

## PHASE 3: COMPLETE FOUR-DIRECTION REMOTE NAVIGATION

Implement reliable navigation throughout the Home and Live TV screens.

### LEFT and RIGHT

- Move focus between adjacent channel cards.
- Navigate horizontally through channel rows.
- Scroll the row automatically when the focused item approaches the viewport edge.
- Keep the focused channel fully visible.
- Allow focus to move from the main content to the sidebar at appropriate boundaries.
- Support horizontal navigation across category chips.
- Prevent focus from jumping to unrelated items.

### UP and DOWN

- Move focus between categories, channel rows, and other vertically arranged sections.
- Maintain the nearest or corresponding horizontal position when moving between rows where appropriate.
- Scroll vertically to reveal the focused item.
- Prevent focus loss at the first and last rows.
- Ensure that content outside the visible area remains reachable.

### OK/SELECT

- Activate the focused card, channel name, category chip, or action button.
- Execute each action exactly once.
- Provide immediate visual feedback.
- Open or play the correct channel using the existing playback implementation.

### BACK

- Close overlays and dialogs first.
- Return from playback to the correct screen.
- Restore focus to the originating channel or control whenever possible.
- Preserve the expected navigation history.

Do not consume every remote key globally. Use the native focus system and targeted focus overrides appropriate to the existing framework.

## PHASE 4: DESIGN A PREMIUM GLASSMORPHISM TV INTERFACE

Upgrade the visual design to a modern, premium television experience inspired by polished streaming interfaces.

Use glassmorphism selectively and consistently.

### Visual style

- Dark cinematic background.
- Translucent glass panels for suitable navigation and information surfaces.
- Subtle background blur where supported and performant.
- Semi-transparent surfaces with tasteful borders.
- Soft shadows and restrained highlights.
- Clear typography and readable channel names.
- Consistent spacing, alignment, and card dimensions.
- Modern rounded corners.
- High-contrast focus and selected states.
- A cohesive accent color consistent with the existing brand.

Avoid excessive transparency, bright outlines everywhere, heavy blur effects, and cluttered layouts.

The glass effect must never reduce text readability or make focus difficult to distinguish.

### Sidebar

- Use a refined dark glass-style navigation panel.
- Maintain clear separation between the sidebar and main content.
- Highlight the focused and selected navigation items distinctly.
- Ensure the sidebar remains fully usable with the remote.
- Preserve the existing Home and Live TV destinations.

### Main content area

- Improve the visual hierarchy of channel categories, channel names, and channel cards.
- Use consistent card spacing and alignment.
- Ensure that the right-side content is fully navigable using the remote.
- Keep focused cards visible while scrolling.
- Avoid clipped text, overlapping cards, and unnecessary empty space.

Apply the glass effect only to surfaces where it improves the experience. Do not blur the entire screen or every card unnecessarily.

## PHASE 5: IMPLEMENT BUTTERY-SMOOTH GLASSMORPHISM ANIMATIONS

Create polished, responsive transitions that feel natural on Android TV hardware.

### Focus animation

When the user navigates to a card:

- Scale the card up subtly.
- Increase elevation or shadow slightly.
- Animate the border and surface opacity smoothly.
- Transition the channel title or metadata if appropriate.
- Keep the animation synchronized with actual focus changes.

Suggested focus animation duration: approximately 120–220 ms.

### Selection animation

When the user presses OK/SELECT:

- Provide a brief, subtle press or confirmation effect.
- Prevent the animation from delaying the action.
- Navigate or initiate playback promptly.
- Avoid duplicate selection events.

### Screen transitions

Implement tasteful transitions between:

- Home and Live TV.
- Sidebar navigation destinations.
- Channel browsing and playback.
- Dialogs and overlays.

Use short, purposeful transitions that do not interfere with focus restoration.

### Animation rules

- Do not block remote input during animations.
- Handle rapid directional presses smoothly.
- Avoid excessive bouncing, large zoom effects, flickering, and layout shifts.
- Prevent multiple animations from fighting over the same properties.
- Respect the lifecycle of screens and UI components.
- Reduce or remove expensive effects when hardware performance requires it.

The interface must feel smooth because the underlying navigation and rendering are efficient, not because of excessive animation.

## PHASE 6: FIX HORIZONTAL SCROLLING AND CHANNEL BROWSING

Audit all horizontal channel rows and category lists.

Requirements:

- LEFT and RIGHT must navigate between selectable items.
- The horizontal list must scroll as focus moves.
- The focused channel must never disappear behind the viewport boundary.
- Scrolling must not conflict with card scaling or elevation.
- Long lists must remain responsive.
- Category changes must update the content correctly.
- Focus must be restored to a sensible item after filtering.
- Vertical scrolling must continue working when multiple horizontal rows exist.

Ensure that scroll position, focus position, and selected channel state remain synchronized.

## PHASE 7: PROFESSIONAL LOADING EXPERIENCE

Replace the current unpolished loading behavior with a clean, professional loading experience.

- Use the application's existing visual identity.
- Add a subtle progress indicator.
- Use restrained glass-style elements if appropriate.
- Display useful status text only when necessary.
- Avoid blank screens and flickering.
- Avoid artificial loading delays.
- Keep expensive work off the main thread.
- Show useful error and retry states if channel data cannot load.
- Hide the loading screen when the actual loading operation completes.

Do not display a permanent loading animation when data is ready or when an error has occurred.

## PHASE 8: PERFORMANCE AND ANDROID TV COMPATIBILITY

Optimize the application for real Android TV devices.

- Avoid unnecessary UI redraws.
- Optimize image and logo loading.
- Reuse list items and views appropriately.
- Prevent memory leaks and duplicate listeners.
- Avoid expensive blur calculations on every animation frame.
- Keep focus movement responsive during scrolling.
- Avoid blocking the main thread.
- Handle slow networks and missing channel logos.
- Preserve compatibility with the project's existing Android SDK and architecture.

Target stable 60 FPS where the hardware supports it, without sacrificing usability on lower-powered devices.

## PHASE 9: TEST THE COMPLETE USER EXPERIENCE

Build and test the application after implementation.

Verify the following using an emulator or actual Android TV when available:

1. Application launch and loading screen.
2. Home screen sidebar navigation.
3. Movement from the sidebar to the main content.
4. Focus movement between channel cards.
5. Selecting channels by focusing their names or card surfaces.
6. Correct OK/SELECT behavior.
7. LEFT/RIGHT horizontal scrolling.
8. UP/DOWN movement between rows.
9. Focus visibility during scrolling.
10. Glassmorphism and focus animations.
11. Channel playback and error handling.
12. BACK navigation and focus restoration.
13. Rapid remote input and repeated selections.
14. Performance and runtime logs.

Use actual test results. Do not claim that the application works on a physical TV if it has only been inspected in source code.

## IMPLEMENTATION RULES

- Inspect before modifying.
- Fix root causes rather than applying superficial workarounds.
- Preserve existing functionality and data sources.
- Do not invent channel data or playback success.
- Do not replace the entire architecture unnecessarily.
- Keep changes focused, maintainable, and compatible with the existing project.
- Ensure all essential interactions work without a mouse or touchscreen.
- Prioritize correct navigation and responsiveness over decorative effects.

## FINAL DELIVERABLES

Provide:

1. Root causes of the channel-selection and remote-navigation issues.
2. A list of modified files.
3. Details of the glassmorphism design and animation improvements.
4. Details of the loading-screen improvements.
5. Build and test results.
6. Any remaining issues and recommended next steps.

## START IMPLEMENTATION NOW

Inspect the current application and reproduce the channel-selection problem first.

Then implement the complete navigation, selectable channel cards and names, smooth horizontal scrolling, professional glassmorphism styling, responsive animations, and polished loading experience.

**Final acceptance criterion:** A user must be able to operate the entire Home and Live TV experience using only a physical Android TV remote. Channel names and cards must be selectable, all four directional buttons must behave predictably, scrolling must keep focused items visible, and the interface must feel premium, responsive, and buttery smooth.