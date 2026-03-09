# Repository Readiness

This project directory is prepared to be extracted into its own repository.

## Included repo-level files

- `mvnw`, `mvnw.cmd`, `.mvn/`
- `.gitignore`, `.editorconfig`
- `LICENSE`, `NOTICE`, `CHANGELOG.md`
- `CONTRIBUTING.md`, `SECURITY.md`
- `.github/workflows/ci.yml`, `.github/workflows/release.yml`
- `.github/ISSUE_TEMPLATE/`
- `.github/pull_request_template.md`
- `scripts/extract-repository.ps1`

## Extraction checklist

- Run `scripts/extract-repository.ps1 -Destination <path> -InitGit` or copy the directory contents manually
- Push the extracted repository to GitHub
- Enable Actions for the repository
- Review project name, groupId, and package coordinates before first public release
- Replace demo endpoints and tokens with real secret management before production use


