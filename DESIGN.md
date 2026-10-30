# Coroutines Tests Mini: screen design

A deliberately small Views state demonstrator. One vertical reading path presents the question, state sequence, current status, result and load action. It keeps the educational scope small while giving Idle/Loading/Success/Error an explicit hierarchy. No extra pages or simulated network controls are introduced.

## Visual system

Light: paper #FAF8F3, ink #202421, muted #5E625C, copper #9B432F, soft #F2E3DB, outline #85877E. Dark: paper #191D1A, ink #F1EEE7, muted #C7C9C2, copper #EDB7A4, soft #4B3026, outline #96998F. A single copper accent adapts to system dark mode; no dynamic palette overrides it. Body contrast is 14.81:1 light/14.71:1 dark, secondary text 5.86:1/10.20:1 and primary button 6.49:1/9.30:1. State is identified by words as well as color.

Local Outfit handles titles, labels and reading. Controls have explicit 12 dp corners, minimum 48 dp actions and native touch feedback. Gutters 20 dp; section spacing 20–32 dp. Open sections and rules replace repeated cards. Text wraps and scrolls with available space; large text is not shrunk to fit. No remote font provider, analytics or decorative animation is added. ES resources have an English fallback. Full font attribution is in THIRD_PARTY.md and the packaged OFL notice.

## Interaction and ownership

The load action renders Loading immediately and remains disabled until completion, preventing repeated taps from creating simultaneous requests through this screen. It becomes available again with a Load again label. The existing ViewModel owns the request and its result across activity recreation. The underlying domain remains unchanged; the separate search ViewModel demonstrates replacement in JVM tests.

The result is a polite live accessibility region with related text grouped for screen readers. The title is a heading. The progress indicator is decorative because the state is announced in text. Insets handle system bars/cutouts; NestedScrollView makes the same content usable with large text and landscape. Only native ripple/progress motion is used. The installed fixture always succeeds with the original fixed English article; error/cancellation behavior is checked through fakes rather than advertised as a UI scenario.

TextAppearance selects each Outfit weight explicitly. A global theme font would override those role-specific choices in Views; runtime checks confirm title weight 700 and state weight 600 at both standard and 150% text.

## Verification

See [VERIFICATION.md](docs/VERIFICATION.md) for dated test/build results, native journeys, screenshots and limits. The current editorial review replaces vague headings with specific experiments and actions. API names and stable trace messages remain identifiable.
