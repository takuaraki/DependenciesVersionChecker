package entities.repositories;

/**
 * Google repository
 */
public class GoogleRepository extends Repository {
    public GoogleRepository() {
        super("file://" + System.getProperty("user.home") + "/Library/Android/sdk/extras/google/m2repository/");
    }
}
