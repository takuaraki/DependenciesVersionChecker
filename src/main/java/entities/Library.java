package entities;

import dtos.Dependency;

/**
 * Library declared in build.gradle
 */
public class Library {
    public enum Status {
        CURRENT,
        EXCEED,
        OUTDATED,
        UNRESOLVED;
    }

    String groupId;
    String artifactId;
    String currentVersion;
    String latestVersion;

    Status status;

    private Library() {
    }

    public static Library create(Dependency dependency, Status status) {
        Library library = new Library();
        library.groupId = dependency.group;
        library.artifactId = dependency.name;
        library.currentVersion = dependency.version;
        library.status = status;

        switch (status) {
            case CURRENT:
                library.latestVersion = dependency.version;
                break;
            case EXCEED:
                library.latestVersion = dependency.latest;
                break;
            case OUTDATED:
                library.latestVersion = dependency.available.milestone;
                break;
            case UNRESOLVED:
                library.latestVersion = "";
                break;
        }

        return library;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public Status getStatus() {
        return status;
    }
}
