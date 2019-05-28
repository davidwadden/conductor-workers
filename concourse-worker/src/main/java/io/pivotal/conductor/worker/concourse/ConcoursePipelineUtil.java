package io.pivotal.conductor.worker.concourse;

public class ConcoursePipelineUtil {

    private ConcoursePipelineUtil() {}

    static String derivePipelineName(String projectName) {
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
