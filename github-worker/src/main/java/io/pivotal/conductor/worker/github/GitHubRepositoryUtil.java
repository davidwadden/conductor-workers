package io.pivotal.conductor.worker.github;

class GitHubRepositoryUtil {

    private GitHubRepositoryUtil() {}

    static String deriveRepositoryName(String projectName) {
        return sanitizeProjectName(projectName);
    }

    private static String sanitizeProjectName(String projectName) {
        return projectName
            .replaceAll("[\\s]", "-")
            .replace("_", "-")
            .replace(".", "-")
            .replaceAll("[^a-zA-Z0-9\\-]", "")
            .toLowerCase();
    }

}
