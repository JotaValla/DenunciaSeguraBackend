# Repository Guidelines

## Project Structure and Module Organization

- Root Maven aggregator with four Spring Boot modules: `ms-configServer`, `ms-eureka`, `ms-gateway`, `ms-denuncias`.
- Each module follows `src/main/java` and `src/main/resources` for application code and config.
- Tests live in `src/test/java` under each module (currently minimal/empty).
- Centralized config lives in `ms-configServer/src/main/resources/configurations/` (for example `ms-denuncias.yaml`).
- OpenAPI contracts are in `docs/api-contracts/`.

## Build, Test, and Development Commands

- `mvn clean package` builds all modules from the root.
- `mvn -pl ms-eureka -am spring-boot:run` runs a specific service (use `ms-configServer`, `ms-eureka`, `ms-gateway`, `ms-denuncias`).
- `mvn -pl ms-denuncias -am test` runs tests for one module and its dependencies.
- Typical local order: start `ms-configServer`, then `ms-eureka`, then `ms-gateway`, then `ms-denuncias`.

## Coding Style and Naming Conventions

- Java 25, Spring Boot 4.x, Spring Cloud 2025.1.x.
- Indentation: 4 spaces in Java, 2 spaces in YAML.
- Package naming: `com.andervalla.<module>`; classes in `PascalCase`, methods/fields in `camelCase`.
- Keep configuration keys in `kebab-case` or `lowercase` as used by Spring (`spring.application.name`).

## Testing Guidelines

- Framework: JUnit (via `spring-boot-starter-test` and module-specific test starters).
- Name tests with `*Test` and place them under `src/test/java` in the matching package.
- No explicit coverage thresholds yet; add focused unit tests for new logic.

## Commit and Pull Request Guidelines

- Commit messages follow a conventional style seen in history: `feat: ...`, `build: ...`, `docs(scope): ...` with short Spanish descriptions.
- PRs should include: a brief summary, how to run/verify, and any config changes (especially config server files).

## Security and Configuration Tips

- Do not hard-code secrets in configs. Use environment variables or externalized config where possible.
- If you change service ports or URLs, update both the service config and any gateway routes.
