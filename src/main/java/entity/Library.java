package entity;

/**
 * Library available from maven repository.
 */
public class Library {
    String groupId;
    String artifactId;
    String currentUsingVersion;
    String metaDataUrl;

    public Library(String groupId, String artifactId, String currentUsingVersion) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.currentUsingVersion = currentUsingVersion;
        metaDataUrl = null;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getCurrentUsingVersion() {
        return currentUsingVersion;
    }

    public String getMetaDataUrl() {
        return metaDataUrl;
    }

    public void setMetaDataUrl(String metaDataUrl) {
        this.metaDataUrl = metaDataUrl;
    }
}
