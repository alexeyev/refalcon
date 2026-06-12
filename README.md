# Refalcon — a Refal-5λ plugin for IntelliJ IDEA

An IntelliJ Platform plugin that makes IntelliJ IDEA **recognize, highlight, and run
Refal-5 Lambda** source files (`.ref`, `.refi`).

## Features

**Syntax highlighting** (lexer-based):
- directives / keywords: `$ENTRY`, `$EXTERN`, `$EXTRN`, `$EXTERNAL`, `$EASTEREGG`,
  `$ENUM`, `$EENUM`, `$SWAP`, `$ESWAP`, `$SCOPEID`, `$DRIVE`, `$INLINE`, `$SPEC`, `$INCLUDE`
  (unknown `$WORD`s are flagged)
- variables `s.X` / `t.X` / `e.X`
- single- and double-quoted strings, **with escape highlighting** inside them
  (`\n \r \t \\ \' \" \< \> \( \)` and `\xHH` shown as valid; anything else as an invalid escape)
- numbers (macrodigits)
- line comments (`*` in the first column) and block comments (`/* … */`)
- embedded native blocks (`%% … %%`)
- function **definitions** (a name immediately before `{`) vs function **calls**
  (a name immediately after `<` or `[`)
- brackets `( )`, `{ }`, `< >`, `[ ]`, plus `;` `,` `:` `=`

**Editor integration:**
- a configurable **Color Settings** page (Settings → Editor → Color Scheme → Refal-5 Lambda)
- **brace matching** for `{ } ( ) < > [ ]`
- **comment toggling** (block comments via Code → Comment with Block Comment)

**Code intelligence** (PSI/parser-based):
- a **Structure view** (the file-structure popup, Ctrl/Cmd+F12) listing the functions defined in the file
- **code folding** for function bodies `{ … }`, block comments, and `%% … %%` native blocks
- basic **code completion** (Ctrl+Space) for directive keywords, common built-in functions,
  function names defined in the current file, and the `s.`/`t.`/`e.` variables in scope
- **navigation & refactoring**: Go to Declaration (Ctrl/Cmd+click a call jumps to its definition —
  in the same file, or another project file declared via `$EXTERN`), **Go to Symbol**
  (Ctrl+Alt+Shift+N / Cmd+Opt+O — jump to any function in the project by fuzzy name),
  Find Usages (Alt/Opt+F7), and
  Rename (Shift+F6) — renaming a function updates all of its calls across the project
- **Unreachable-sentence inspection**: Refal tries sentences in order, and a lone `e.X`
  pattern matches anything — sentences after it are dead code. Flagged narrowly: parentheses,
  extra variables/symbols, or a where-clause make the pattern fallible and are never reported
- **Unused function inspection** (Settings → Editor → Inspections → Refal): grays out functions
  nothing calls — conservatively: `$ENTRY` functions and `Go`/`GO` are exported/entry API and are
  never flagged, and cross-file usage is checked before reporting; also runs in batch via
  Code → Inspect Code
- **formatting**: Reformat Code (Ctrl+Alt+L / Cmd+Opt+L) with the canonical 2-space Refal style;
  continuation lines (leading `=` / `,`) indent one extra level, spacing around `=` `:` `,` `;`
  and inside `< >` `( )` is normalized, and your line structure is preserved. Column-0 `*`
  comments are pinned — indenting them would turn them into code, so the formatter never does
- **quick documentation** (Ctrl/Cmd+Q, or hover): built-in functions show a short description;
  functions defined in the file show their header
- **instant error highlighting** (no compiler needed) with **quick-fixes** — Alt+Enter on an
  unresolved call offers *Create function* or *Add `$EXTERN`*: an undefined function call (not a built-in,
  standard-library, defined, or `$EXTERN`-declared name) is flagged as *Unresolved function*, and a
  variable used on the result side that the pattern never binds is flagged as *Unresolved variable*.
  This works as you type and complements the full `rlc` diagnostics below. The idea is borrowed from
  the Grammar-Kit-based coursework plugin
  [bmstu-iu9/RefalFiveLambdaPlugin](https://github.com/bmstu-iu9/RefalFiveLambdaPlugin)
  (re-implemented here on this plugin's own grammar, kept conservative to avoid false positives).

**Run support:** a "Refal-5λ" run configuration that compiles a `.ref` file with the Refal-5λ
compiler (`rlc`) and, by default, runs the resulting executable — output streams to the Run console.
The compiler is **auto-detected** on your `PATH` (and common install dirs), a green **run arrow**
appears next to the `Go`/`GO` entry function, and compiler diagnostics like `file.ref:line:col`
become **clickable links** in the Run console (absolute Windows paths included). If `rlc` is
found, the compiler also runs a fast `--grammar-check` (syntax-only, no artifacts, no C++
toolchain needed) in the background and shows its **errors inline** in the editor.

**Boilerplate:** a *New → Refal File* entry that seeds a runnable skeleton, plus live templates —
type `entry`, `fn`, or `prout` and press Tab to expand the program entry point, a function, or a
`<Prout …>` call.

## Dialects: classic Refal-5 and Refal-2

Refalcon targets Refal-5λ, but `.ref` belongs to the whole family, so the plugin understands
its relatives too:

- **Classic Refal-5** works as-is — the λ syntax is a superset for editor purposes. `$ENTRY` /
  `$EXTRN` / `$EXTERNAL` directives, `s.X`/`t.X`/`e.X` variables and column-0 `*` comments are
  native here, and the classic built-ins (`Card`, `Print`, `Mu`, `Br`, `Dg`, …) are already in
  the completion list and the resolver's known set.
- **Refal-2** files are detected by content, because extensions cannot help: the historic
  refal2-0.2.3 distribution uses `.ref` as well (verified against its sources). A `name start`
  module header, bare `entry`/`extrn` directives, or a `k/name/` call switch the file to a
  dedicated Refal-2 lexer and parser; any `$`-directive vetoes the switch. What works:
  full highlighting (`/123/` macrodigits, `s1s2s3` variable chains, `V(D)X` specifier
  variables, `+` line continuations, both call notations `<Name …>` and `k/name/ … .`),
  brace matching for `( )`, `< >` and `k/ … .`, the structure view, comment toggling, find
  usages, rename, and **case-insensitive** go-to-definition (`extrn print` ↔ `<Print …>`,
  exactly as the real sources do) — in-file and across the project's Refal-2 files. Refal-2
  functions also appear in Navigate → Symbol….

Honest limits for Refal-2: no completion, formatter, run configuration, or λ-specific
inspections — dialect support is deliberately compiler-independent. Rename and find-usages
match exact case (the word index is case-sensitive) while navigation resolves
case-insensitively. Grounding: the refal2-0.2.3 distribution's test programs and `xcv.ref`,
plus its manual.

## Requirements

- IntelliJ IDEA **2024.3 or newer** (Community or Ultimate); it also loads on 2025.x+.
- **JDK 21** to build (the Gradle wrapper auto-provisions Gradle 9.5.1; a JDK 21 toolchain can be
  auto-downloaded if you don't have one).
- To actually run programs: the **Refal-5λ toolchain** (`rlc` / `rlmake`) on your machine —
  see https://github.com/bmstu-iu9/refal-5-lambda.

## Get IntelliJ IDEA Community Edition (open source, from GitHub)

JetBrains publishes IntelliJ IDEA Community Edition as Apache-2.0 **source code** (not prebuilt
binaries) at [github.com/JetBrains/intellij-community](https://github.com/JetBrains/intellij-community).
To get a runnable IDE you build it:

1. Pick the branch for the version you want — branch names match build numbers, e.g. `243` for
   2024.3 (`master` is the next major release).
2. Shallow-clone that branch (saves a lot of time):
   `git clone --depth 1 -b 243 https://github.com/JetBrains/intellij-community.git`
3. Build the installers with `installers.cmd` (works on Windows and Unix; a Docker build is also
   provided), **or** open the sources in an existing IntelliJ and use *Build → Build Project* plus
   the preconfigured **IDEA** run configuration to launch it.

(The ready-made installers on jetbrains.com are the same product through a separate, non-GitHub
channel.)

## Install the plugin

Get `refalcon-<version>.zip` — from a GitHub Release, or build it (see below). Then in the IDE:
**Settings → Plugins → ⚙ → Install Plugin from Disk…**, choose the zip, and restart. Open a `.ref`
file such as `examples/hello.ref` to confirm highlighting works.

## Build from source

```bash
./gradlew buildPlugin           # -> build/distributions/refalcon-<version>.zip
```

This produces the installable zip (install it as above). To try the plugin in a throwaway sandbox
IDE instead, run `./gradlew runIde`.

## Install from a plugin repository (one-click, with auto-updates)

For installs and updates without the Marketplace (and without a JetBrains account), the project
publishes its own **custom plugin repository** via GitHub Releases + GitHub Pages.

One-time setup (maintainer): in the repo, enable **Settings → Pages → Source: GitHub Actions**.
Then each `git tag vX.Y.Z && git push --tags` runs `.github/workflows/release.yml`, which builds the
plugin, attaches the zip to a GitHub Release, and publishes an `updatePlugins.xml` to Pages at
`https://<owner>.github.io/<repo>/updatePlugins.xml`.

One-time setup (user): **Settings → Plugins → ⚙ → Manage Plugin Repositories…**, add that
`updatePlugins.xml` URL, then install "Refalcon" from the Marketplace tab. After that the IDE
offers updates automatically whenever you publish a new tag.

> Before publishing, set the `<vendor>` (and optionally its `url`) in
> `src/main/resources/META-INF/plugin.xml`.

## Install the Refal compiler (rlc)

The plugin itself needs nothing extra — highlighting, completion, navigation and the instant
error checks all work out of the box. Installing `rlc` additionally enables the inline compiler
diagnostics and the Run button.

**Windows (easiest):** download `setup-refal-5-lambda-<version>.exe` from the
[latest release](https://github.com/bmstu-iu9/refal-5-lambda/releases/latest) and run it — it
unpacks the toolchain into your user profile and adds it to your `PATH`. **No C++ compiler is
needed on Windows**: by default programs link against prebuilt runtime *prefixes* (`slim`/`rich`)
shipped with the installer (verified from the toolchain's own `rlc.bat`, which passes
`--exesuffix=.exe` and an empty C++ command in this mode). **Then restart the IDE**: a running
IDE keeps the `PATH` it started with and will not see a freshly installed `rlc`.

**Linux / macOS:**

macOS is officially supported (the toolchain's own CI builds on it); building needs a C++
compiler — on macOS install the Xcode Command Line Tools first (`xcode-select --install`),
on Linux `g++` from your package manager.

```bash
git clone https://github.com/bmstu-iu9/refal-5-lambda
cd refal-5-lambda && ./bootstrap.sh     # builds the toolchain with your C++ compiler
# then add its bin/ to PATH (the official docs also set RL_MODULE_PATH to the same dir)
```

If you clone into your home directory, the plugin auto-detects `~/refal-5-lambda/bin` (and
`~/simple-refal-distrib/bin`) even before you update `PATH`.

Open a **new** terminal and type `rlc` to verify (it prints usage). The plugin **auto-detects**
`rlc` on the `PATH` and in standard install locations; you can always override it per run
configuration via the **"Refal compiler (rlc)"** field.

**Troubleshooting — `'rlc' is not recognized as an internal or external command`:** on Windows the
toolchain ships **`rlc.bat`** (no `rlc.exe`), installed to **`%APPDATA%\Refal-5-lambda\bin`** —
verified by inspecting the official installer. The plugin auto-detects it there *even if the
IDE's `PATH` is stale*. If the shell still can't find it: restart the IDE after installing —
and **restart JetBrains Toolbox too** if you launch through it (IDEs inherit Toolbox's old
`PATH`) — or put the full path (e.g. `%APPDATA%\Refal-5-lambda\bin\rlc.bat`) into the run
configuration's compiler field. On a genuine "command not found" exit, the Run console now
appends these exact instructions.

## Running a Refal program

The Refal-5λ compiler turns a `.ref` file into a native OS executable (no separate interpreter).
This plugin's run configuration drives that:

1. Open a `.ref` file (or right-click it / press Ctrl+Shift+F10) and choose **Run** — a
   "Refal-5λ" configuration is created with the file prefilled.
2. In the configuration you can set:
   - **Refal compiler (rlc)** — path to the compiler. Leave it **empty to auto-detect**
     (`PATH` + standard install locations); set it only to pin a specific binary.
   - **Build with rlmake** — check this for multi-file programs: `rlmake` follows `*$FROM Unit`
     comments, compiles every dependent unit automatically, and links a single executable named
     after the main file (verified against the real toolchain; on Windows `rlmake.bat` ships
     alongside `rlc.bat` and is auto-detected the same way).
   - **Compiler options** — extra flags passed to the compiler.
   - **Refal file** — the `.ref` source.
   - **Run the compiled executable after a successful compile** — when on, the produced binary is
     executed right after compilation.
   - **Output executable** — the produced binary (defaults to the source base name, run as
     `./name` on macOS/Linux or `name.exe` on Windows).
   - **Program arguments** — passed to your compiled program.
   - **Working directory** — defaults to the source file's folder.

When "run after compile" is on, the two steps are chained through your OS shell
(`rlc … && ./program …`). Turn it off to only compile. If your build is more involved, point the
compiler field at `rlmake` or at your own script.

## Verify the plugin works

Install the plugin (above), then open the bundled examples: `examples/hello.ref` (a small, runnable
program) and `examples/showcase.ref` (which deliberately contains the tricky cases). Use
`hello.ref` unless a step says otherwise.

1. **Highlighting** — `$ENTRY`/`$EXTERN` (keyword), `s.N`/`e.Name` (variables), strings, numbers,
   `*` and `/* */` comments, and function names vs. calls are all colored.
2. **Escapes** — in a string, `\n` `\t` `\x41` show the *valid-escape* color; open `showcase.ref`
   to see `\q`/`\x` in the *invalid* color and `$FOOBAR` flagged as an unknown directive.
3. **Brace matching** — put the caret next to `{`, `(`, `<`, or `[`; its partner is highlighted.
4. **Folding** — click the gutter arrow by a `{` to fold a function body; block comments and the
   `%% %%` block (in `showcase.ref`) fold too.
5. **Structure view** — press Ctrl/Cmd+F12: you should see `Go`, `Fact`, `Greet`.
6. **Completion** — type `<` then Ctrl+Space: the list offers `$`-directives, common built-ins
   (`Prout`, `Mul`, `Sub`, `Symb`, …), and the file's own function names.
7. **Run** — right-click `hello.ref` → *Run 'hello'*. With `rlc` on your `PATH` it compiles and
   runs, printing the greeting and `Factorial of 5 = 120` in the Run console. (A green arrow also
   appears in the gutter next to `Go`.)
8. **Navigation** — Ctrl/Cmd+click the `<Fact …>` call to jump to the `Fact` definition;
   press Shift+F6 on `Fact` to rename it and watch the call update; Alt/Opt+F7 lists its usages.
9. **Docs & templates** — press Ctrl/Cmd+Q on `<Prout …>` to see its description; in a new file,
   type `entry` then Tab to expand the program skeleton (and try *New → Refal File* on a folder).

## Project layout

```
src/main/java/com/github/refal5lambda/
  RefalTokenKind.java / RefalScanner.java   pure, IntelliJ-free, unit-testable scanner
  RefalLexer.java                            LexerBase adapter (carries string state)
  RefalTokenTypes.java                       IElementType per kind + kind→type map
  RefalLanguage.java / RefalFileType.java    language + .ref/.refi file type
  RefalSyntaxHighlighter*.java               token → color mapping + factory
  RefalColorSettingsPage.java                color settings UI
  RefalBraceMatcher.java / RefalCommenter.java
  RefalStructureViewFactory/Model/Element.java  structure view
  RefalFoldingBuilder.java                     code folding
  RefalCompletionContributor.java / RefalBuiltins.java   completion
  psi/  parser, ParserDefinition, PSI nodes (RefalFile, RefalFunction, RefalBlock, RefalDirective)
  RefalIcons.java
  run/                                       run configuration (type, factory, settings, producer)
src/main/resources/META-INF/plugin.xml
src/main/resources/icons/refal.svg
src/test/java/.../RefalScannerTest.java       pure scanner unit tests (JUnit 4)
src/test/java/.../psi/RefalParsingTest.java   PSI parsing test (ParsingTestCase)
src/test/testData/Hello.ref, Hello.txt        parsing-test input + expected PSI tree
examples/hello.ref, examples/showcase.ref     sample programs (see "Verify the plugin works")
.github/workflows/build.yml, release.yml      CI: build/test/verify, and tagged releases
```

## Design notes & limitations

**Scalability** (measured against the largest real Refal-5λ codebase — the
[compiler's own repository](https://github.com/bmstu-iu9/refal-5-lambda), 901 `.ref`/`.refi`
files, ~1.3 MB): the hand-written lexer tokenizes that entire corpus in ~17 ms on commodity
hardware, and highlighting is per-file and incremental anyway. Find Usages and Rename are backed
by the IDE word index (only candidate files are parsed). Cross-file Go to Declaration and
Go to Symbol share one cached project-wide symbol map, rebuilt lazily after a PSI change rather
than rescanning the project per query. The plugin has no stub index — on a cold start or right
after an edit, the first cross-file navigation rebuilds the map by walking project Refal files
once; instant at these scales, but a stub-based index would be the next step for codebases of
many thousands of files. The inline compiler check (`rlc --grammar-check`) is per-file and took
0.25 s on the project's largest file (107 KB); it does not process `$INCLUDE`, so multi-file
projects get no spurious include errors from it (verified against the real compiler).

- Parsing is intentionally **lenient**: it never reports syntax errors, so valid code is never
  shown as red. Function bodies are modelled as sentences (`pattern = result ;`) with parenthesized
  groups, nested activations (`<…>`/`[…]`), and nested blocks; conditions and where-clauses are
  tolerated rather than fully structured.
- The lexer is mostly **stateless**; only string literals carry state (so escapes can be split out).
- Inline `rlc` diagnostics: the editor text is compiled with `rlc` in an isolated temp directory
  (so the project tree is untouched and unsaved edits are checked), and diagnostics — emitted as
  `file:line:col: ERROR: message`, a format verified against the current `rlc` — are shown inline.
  Because the check is isolated, files that `$INCLUDE` siblings may show spurious errors. If `rlc`
  isn't found, nothing is shown. The invocation and parsing live in `RefalExternalAnnotator` and
  `RefalDiagnosticParser`.
  A newline always resets the state, so incremental re-highlighting stays cheap and correct.
- **Function definition vs call** is a lexer-level heuristic (preceding `<`/`[` ⇒ call, following
  `{` ⇒ definition), not a full parser; unusual layouts may be mis-tagged but still get a sensible
  color.
- The **`%% … %%`** block is one highlighted region; it does not run a real C++ highlighter inside
  (IDEA Community has no C++ support to delegate to).
- The line-comment prefix `*` is only a comment in column 0. IntelliJ places line comments at
  column 0 by default, so *Comment with Line Comment* matches Refal's rule.
- The run configuration's compile-and-run uses simple shell chaining with basic path quoting; for
  exotic paths/arguments, run compile and execution as separate configurations.

## Tests & CI

- `./gradlew test` runs the unit tests: fast, IntelliJ-free tests for the scanner
  (`RefalScannerTest`) plus a `ParsingTestCase` (`RefalParsingTest`) that checks the produced PSI
  tree against `src/test/testData/Hello.txt`.
- `./gradlew verifyPlugin` runs the JetBrains Plugin Verifier against the baseline IDE to check
  binary compatibility and plugin-descriptor validity.
- GitHub Actions (`.github/workflows/build.yml`) runs `check buildPlugin` and the verifier on every
  push and pull request and uploads the built zip; `release.yml` builds the plugin and attaches the
  zip to a GitHub Release when you push a `vX.Y.Z` tag.

## Changing the target IDE

Edit `build.gradle.kts`: `intellijIdeaCommunity("2024.3")` (or `intellijIdeaUltimate("…")`) and the
`sinceBuild` / `untilBuild` under `pluginConfiguration { ideaVersion { … } }`.

## Credits & license

Token rules were informed by the Refal-5λ language and the community VS Code extension
[GDVFox/vscode-refal-5-lambda](https://github.com/GDVFox/vscode-refal-5-lambda); the compiler is
[bmstu-iu9/refal-5-lambda](https://github.com/bmstu-iu9/refal-5-lambda). A separate IDEA plugin also
exists — [bmstu-iu9/RefalFiveLambdaPlugin](https://github.com/bmstu-iu9/RefalFiveLambdaPlugin), a 2020
student coursework (BMSTU, dept. iu9) by Daria Tereshkina and Alexander Konovalov that builds its
lexer and parser with JFlex and Grammar-Kit. This project is an independent implementation, and its
**instant, compiler-free error highlighting** (*Unresolved function* / *Unresolved variable*) is
inspired by that coursework — re-implemented here on this plugin's own grammar rather than copied.
All Java here was written from scratch for the IntelliJ Platform. Use and modify it freely; if you
publish it, add your own license and plugin id.
