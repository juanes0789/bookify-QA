# Sonar integration

The project now runs Sonar analysis through Azure DevOps. The GitHub Actions Sonar workflow was removed because the primary CI/QA pipeline is managed in Azure and already covers test execution, JaCoCo coverage, and SonarCloud upload.

## Azure DevOps configuration

Configure the following variables/secret in the Azure pipeline or pipeline library:

- `SONAR_PROJECT_KEY`: project key registered in SonarCloud or SonarQube.
- `SONAR_ORGANIZATION`: SonarCloud organization key. It is not required for a self-hosted SonarQube instance.
- `SONAR_TOKEN`: analysis token stored as a secure Azure pipeline variable/secret.
- `SONAR_HOST_URL`: Sonar server URL. Use `https://sonarcloud.io` for SonarCloud.

The Azure QA stage already runs:

```bash
./gradlew clean test jacocoTestReport sonar --no-daemon
```

## Local execution

```bash
SONAR_TOKEN=<token> \
SONAR_PROJECT_KEY=<project-key> \
SONAR_HOST_URL=https://sonarcloud.io \
SONAR_ORGANIZATION=<organization> \
./gradlew sonar
```

For a self-hosted SonarQube server, omit `SONAR_ORGANIZATION` and set `SONAR_HOST_URL` to the server URL.

## Notes

- GitHub Actions should not trigger duplicate Sonar analysis for this repository.
- Keep Sonar configuration only in the Azure pipeline and the Gradle plugin settings.
