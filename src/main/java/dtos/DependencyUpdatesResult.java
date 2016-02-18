package dtos;

import java.util.List;

/**
 * Json report created by gradle-versions-plugin
 */
public class DependencyUpdatesResult {
    public Dependencies current;
    public Dependencies exceeded;
    public Dependencies outdated;
    public Dependencies unresolved;
    public int count;

    public List<Dependency> getCurrentDependencies() {
        return current.dependencies;
    }

    public List<Dependency> getExceededDependencies() {
        return exceeded.dependencies;
    }

    public List<Dependency> getOutdatedDependencies() {
        return outdated.dependencies;
    }

    public List<Dependency> getUnresolvedDependencies() {
        return unresolved.dependencies;
    }
}
