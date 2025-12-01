---
description: Run KtLint formatting with Java 24
---

Run KtLint formatting:

1. Set up Java 24 environment:
   ```bash
   export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home
   ```

2. Run KtLint formatting:
   ```bash
   ./gradlew ktlintFormat
   ```

3. Verify formatting results

4. If failed, analyze error messages and suggest solutions