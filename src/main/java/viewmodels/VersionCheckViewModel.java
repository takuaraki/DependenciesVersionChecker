package viewmodels;

import entity.Library;
import models.LibraryModel;

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
        return libraryModel.getLibraries();
    }
}
