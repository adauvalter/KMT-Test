
## Coding conventions and tech details

Never implement the changes right away. Make a proposal, share a plan and upon confirmation start executing it._
Make sure the approach is holistic, the edge-cases are covered.
Revisit test coverage, add the necessary test cases, review the existing ones and prune or reorganize the ones that are not relevant anymore.
Always prefer adding imports and not using fully qualified names, prefer named arguments in non-trivial method calls.
Reuse Gradle home in the project root in `GRADLE_USER_HOME=./.codex-gradle-home`.
On changes done, make sure there is extensive unit tests coverage for all important edge-cases. Run unit tests and ktlintFormat.
Do not use Perl.
Do not use PowerShell only on Windows host.

### Kotlin

If conditions should always be multiline and with curly brances. Same for non-trivial when blocks.
Do not use double bangs in application code. It is error-prone and is a code smell. It can only be tolerated in tests.