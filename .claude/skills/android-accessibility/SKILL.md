---
name: android-accessibility
description: Compose accessibility audit — content descriptions, 48dp touch targets, WCAG AA contrast ratios, focus semantics, and screen reader support.
---

# Android Accessibility Checklist

This guide provides a structured approach to auditing accessibility in Android apps, particularly those using Jetpack Compose.

## Key Areas Covered

**Content Descriptions**: Images and icons require meaningful descriptions; decorative elements should use `null`, while actionable items should describe the action rather than visual appearance.

**Touch Targets**: Interactive elements need minimum dimensions of "48x48dp for all interactive elements." Developers can use `MinTouchTargetSize` or padding to meet standards.

**Color Contrast**: The standard requires "4.5:1 for normal text and 3.0:1 for large text/icons" per WCAG AA guidelines.

**Focus & Semantics**: Logical keyboard navigation is essential, with `mergeDescendants = true` recommended for grouped elements. Custom states should include `stateDescription` parameters.

**Headings**: Section titles benefit from `Modifier.semantics { heading() }` to enable screen reader navigation.

## Compose Checklist

- [ ] All `Icon` / `Image` composables have `contentDescription` (or `null` if decorative)
- [ ] All clickable composables meet 48×48dp minimum touch target
- [ ] Custom interactive components expose correct `Role` via `Modifier.semantics { role = Role.Button }`
- [ ] Color choices meet WCAG AA contrast ratios (use `MaterialTheme` tokens from `:core:systemdesign`)
- [ ] Screen reader focus order is logical (use `isTraversalGroup` and `traversalIndex` where needed)
- [ ] Loading states are announced via `LiveRegion` or `stateDescription`
