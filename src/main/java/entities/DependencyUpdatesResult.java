package entities;

/**
 * Json report created by gradle-versions-plugin
 */
public class DependencyUpdatesResult {
    public Dependencies current;
    public Dependencies exceeded;
    public Dependencies outdated;
    public Dependencies unresolved;
    public int count;

    @Override
    public String toString() {
        return "count: " + count;
    }
}
