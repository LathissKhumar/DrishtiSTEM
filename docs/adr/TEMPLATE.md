# ADR Template

Use this template for new Architecture Decision Records.

---

# ADR-{NUMBER}: {TITLE}

## Status

{Proposed | Accepted | Deprecated | Superseded by [ADR-XXX](link)}

## Date

{YYYY-MM-DD}

## Context

{What is the issue that we're seeing that is motivating this decision or change? What forces are at play (technical, political, social, project)?}

## Decision

{What is the change that we're proposing and/or doing? Be specific and concrete. Use code examples where appropriate.}

## Consequences

### Positive

- {List positive outcomes}

### Negative

- {List negative outcomes}

### Mitigations

- {How we plan to address negative outcomes}

## Alternatives Considered

### 1. {Alternative Name}

{Description of alternative}

**Rejected:** {Why this was rejected}

### 2. {Alternative Name}

{Description of alternative}

**Rejected:** {Why this was rejected}

## References

- [Related ADR](link)
- [Related documentation](link)
- [Source code](link)

---

## Writing Guide

### What is an ADR?

An Architecture Decision Record captures a significant architectural decision along with its context and consequences.

### When to write an ADR

- Choosing between multiple approaches with trade-offs
- Changing an established pattern
- Adopting a new library or framework
- Changing the public API
- Modifying the build system
- Changing the test strategy

### Naming convention

`{NUMBER}-{short-title}.md`

Numbers are sequential: `001`, `002`, `003`...

Titles use kebab-case: `plugin-architecture`, `kmp-common-main`, `error-handling`.

### File location

All ADRs live in `docs/adr/`.
