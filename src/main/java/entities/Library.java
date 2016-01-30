package entities;

/**
 * Library available from maven repository.
 */
public class Library {
    String groupId;
    String artifactId;
    String version;
    String metaDataUrl;

    public Library(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        metaDataUrl = null;
    }

    public Library(Library library) {
        this.groupId = library.getGroupId();
        this.artifactId = library.getArtifactId();
        this.version = library.getVersion();
        this.metaDataUrl = library.getMetaDataUrl();
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMetaDataUrl() {
        return metaDataUrl;
    }

    public void setMetaDataUrl(String repositoryUrl) {
        this.metaDataUrl = repositoryUrl + getMetaDataPath();
    }

    public String getMetaDataPath() {
        return groupId.replace(".", "/") + "/" + artifactId + "/maven-metadata.xml";
    }
}
