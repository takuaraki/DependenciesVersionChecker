package viewmodels;

import entities.Library;
import models.LibraryModel;
import models.RepositoryModel;
import rx.Observable;

import java.util.List;

/**
 * ViewModel of VersionCheckWindow
 */
public class VersionCheckViewModel {

    RepositoryModel repositoryModel;
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
        repositoryModel.init(gradleScript);
        libraryModel.init(gradleScript);
    }

    public List<Library> getLibraries() {
        return libraryModel.getUsingLibraries();
    }

    public Observable<LibraryModel.GetLatestLibrariesResult> getLatestVersions() {
        return libraryModel.getLatestLibraries();
    }
}
