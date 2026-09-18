# Sonar integration

The project is configured to run static analysis and upload JaCoCo coverage
through the Gradle `sonar` task.

## GitHub Actions configuration

Configure these repository variables and secret:

- `SONAR_PROJECT_KEY`: project key registered in SonarCloud or SonarQube.
- `SONAR_HOST_URL`: Sonar server URL. Use `https://sonarcloud.io` for SonarCloud.
- `SONAR_ORGANIZATION`: SonarCloud organization key. It is not required for a
  self-hosted SonarQube instance.
- `SONAR_TOKEN`: analysis token stored as a repository secret.

The workflow runs on pushes to `main` and on pull requests. The token must be
available for the analysis to upload results.

## Local execution

```bash
SONAR_TOKEN=<token> \
SONAR_PROJECT_KEY=<project-key> \
SONAR_HOST_URL=https://sonarcloud.io \
SONAR_ORGANIZATION=<organization> \
./gradlew sonar
```

For a self-hosted SonarQube server, omit `SONAR_ORGANIZATION` and set
`SONAR_HOST_URL` to the server URL.
