# Stage204 GitHub Compile Hotfix 2

Base: `thaumcraft_legacy_rebuild_STAGE204_GITHUB_AUDIT_HOTFIX_1_19_2`

## Fixed

GitHub `compileJava` failed in `JarTubeInteractionRuntime.java` because Java lambdas may only capture local variables that are final or effectively final.

The local variable `aspect` is reassigned while resolving the phial/jar fallback, then captured by `withStyle(style -> style.withColor(aspect.textColor()))`. This is not legal Java.

The fix snapshots the resolved aspect into:

```java
final Aspect finalAspect = aspect;
```

and uses `finalAspect` inside the lambda.

## Scope

No TC4 behavior changed. This is compile compatibility only.

## Verified

- `python3 scripts/java_syntax_guard.py`
- `python3 scripts/github_static_audit.py`
- `python3 scripts/github_ci_guard.py`
- `python3 scripts/tc4_stage202_jar_tube_interaction_audit.py`
- `python3 scripts/tc4_stage204_jar_tube_edge_cases_audit.py`
