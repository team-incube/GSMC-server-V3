# Custom Commands for GSMC Server V3

## commit

Create Git commits by splitting changes into logical units.

**Rules:**
- Follow commit message conventions (add/update/fix/delete/docs/test/merge)
- Use subject line only (no commit body)
- Do NOT add Claude as co-author
- Split changes into appropriate logical units with multiple commits
- Each commit should have a single responsibility

**Steps:**
1. Check changes with `git status` and `git diff`
2. Categorize changes into logical units (e.g., feature addition, bug fix, refactoring)
3. Group files by each unit
4. For each group:
   - Stage only relevant files with `git add`
   - Write concise commit message following conventions (subject only)
   - Execute `git commit -m "message"`
5. Verify results with `git log --oneline -n [number of commits]`

## format

Run KtLint formatting.

**Steps:**
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