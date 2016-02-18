package viewmodels;

import entities.Library;
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

    public void init(String basePath) {
        libraryModel.init(basePath);
    }

    public Observable<List<Library>> getLibraries(String gradleScript) {
        return libraryModel.getLibraries(gradleScript);
    }
}
