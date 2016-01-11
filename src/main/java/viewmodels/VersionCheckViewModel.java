package viewmodels;

import entity.Library;
import models.LibraryModel;
import rx.Observable;

import java.util.List;

/**
 * ViewModel of VersionCheckWindow
 */
public class VersionCheckViewModel {

    LibraryModel libraryModel;

    public VersionCheckViewModel() {
        this.libraryModel = new LibraryModel();
    }

    /**
     * init VersionCheckViewModel
     *
     * @param gradleScript
     */
    public void init(String gradleScript) {
        libraryModel.init(gradleScript);
    }

    public List<Library> getLibraries() {
        return libraryModel.getUsingLibraries();
    }

    public Observable<LibraryModel.GetLatestLibrariesResult> getLatestVersions() {
        return libraryModel.getLatestLibraries();
    }
}
