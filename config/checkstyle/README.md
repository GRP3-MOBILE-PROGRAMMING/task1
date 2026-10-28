# Code Style (Checkstyle)

Every push is checked against [`checkstyle.xml`](checkstyle.xml). Jenkins fails the build, and
does not produce an APK, if any style **error** is found.

## Run it locally

```bash
./gradlew checkstyle        # Windows: gradlew.bat checkstyle
```

Report: `app/build/reports/checkstyle/checkstyle.html`

The check also runs automatically before every build (`preBuild`) and with `./gradlew check`.

## See violations while typing

1. Android Studio → Settings → Plugins → install **CheckStyle-IDEA**.
2. Settings → Tools → Checkstyle → add `config/checkstyle/checkstyle.xml` and set it active.

## What is enforced

| Rule | Modules | Severity |
|---|---|---|
| Naming: PascalCase classes, camelCase methods, UPPER_SNAKE_CASE constants | TypeName, MethodName, ConstantName, MemberName, ParameterName, LocalVariableName, StaticVariableName, PackageName | error |
| Javadoc on public classes and methods | MissingJavadocType, MissingJavadocMethod, JavadocMethod | error |
| No magic numbers (use named constants) | MagicNumber | error |
| Max line length 100, no tabs | LineLength, FileTabCharacter | error |
| Indentation and whitespace | Indentation, WhitespaceAround, WhitespaceAfter, NoWhitespaceBefore, MethodParamPad, ParenPad, OperatorWrap | warning |
| Javadoc wording, method size, newline at end of file | JavadocStyle, ParameterNumber, MethodLength, NewlineAtEndOfFile | warning |
| TODO / FIXME comments | TodoComment | info (never blocks) |

`maxErrors = 0` and `maxWarnings = 0` in `app/build.gradle.kts`, so errors and warnings both fail the
build. Generated code (`R.java`, `BuildConfig.java`, `build/`) is skipped via
[`suppressions.xml`](suppressions.xml).

## Method naming convention

Checkstyle checks the *format* of names, not their meaning. Team convention: methods that return
whether something is available or valid start with `has`, `can`, `is`, or `check`
(e.g. `hasNetwork()`, `canReachInternet()`). Don't add synonyms like `validateInternetAccess()`.
