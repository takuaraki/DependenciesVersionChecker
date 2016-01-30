package entities.repositories;

/**
 * Android repository
 */
public class AndroidRepository extends Repository {
    public AndroidRepository() {
        super("file://" + System.getProperty("user.home") + "/Library/Android/sdk/extras/android/m2repository/");
    }
}
