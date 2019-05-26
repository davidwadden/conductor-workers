package io.pivotal.conductor.worker.cloudfoundry;

class CloudFoundryUtil {

    private CloudFoundryUtil() {}

    static String deriveSpaceName(String projectName, String spaceNameSuffix) {
        String baseSpaceName = sanitizeProjectName(projectName);
        return String.format("%s-%s", baseSpaceName, spaceNameSuffix);
    }

    static String deriveRouteHostname(String projectName, String hostnameSuffix) {
        String prefix = sanitizeProjectName(projectName);
        return String.format("%s%s", prefix, hostnameSuffix);
    }

    static String deriveDatabaseName(String projectName, String spaceNameSuffix) {
        String prefix = sanitizeProjectName(projectName);
        return String.format("%s-database-%s", prefix, spaceNameSuffix);
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
