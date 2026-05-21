---
name: xml-to-compose-migration
description: Systematic guide for converting XML layouts to Compose — layout/widget mapping tables, attribute translation, interop strategies, and state migration.
---

# XML to Compose Migration Guide

This comprehensive resource provides systematic guidance for converting Android XML layouts to Jetpack Compose while maintaining functionality and adopting modern patterns.

## Key Sections

**Analysis & Planning**: The workflow begins by examining the XML structure, identifying View types and attributes, then deciding between full rewrite or incremental migration strategies.

**Layout Mapping**: The guide offers extensive tables mapping XML containers (LinearLayout → Column/Row, RecyclerView → LazyColumn) and widgets (TextView → Text, EditText → TextField) to their Compose equivalents.

**Attribute Translation**: XML properties like `layout_weight` convert to `Modifier.weight()`, while `visibility="gone"` becomes conditional composition logic.

**Common Patterns**: Detailed examples show conversions for weighted layouts, list migrations, data binding, and ConstraintLayout structures. As the guide states: "Convert `LiveData` observation to `StateFlow` collection or `observeAsState()`."

**Interoperability**: The resource emphasizes gradual migration through ComposeView (embedding Compose in XML) and AndroidView (embedding legacy Views in Compose), enabling team adoption without complete rewrites.

**State Management**: Guidance covers migrating from ViewModel observation patterns to Compose's `collectAsStateWithLifecycle()` and converting click listeners to lambda parameters.

A practical checklist ensures completeness, from layout conversion through accessibility verification and old file cleanup.

## Braintraining Note

This project is **Compose-only** from the start. Use this skill only if consuming a third-party View-based component that needs to be wrapped via `AndroidView`.
