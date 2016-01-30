package entities.repositories;

/**
 * Maven repository
 */
public class Repository {
    private String url;

    public Repository(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
