package entities.repositories;

/**
 * Maven central repository
 */
public class MavenCentralRepository extends Repository {
    public MavenCentralRepository() {
        super("https://repo1.maven.org/maven2/");
    }
}
