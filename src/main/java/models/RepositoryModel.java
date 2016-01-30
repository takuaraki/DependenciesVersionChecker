package models;

import entities.repositories.JCenterRepository;
import entities.repositories.MavenCentralRepository;
import entities.repositories.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Model of Maven repository
 */
public class RepositoryModel {

    List<Repository> repositories;

    public void init(String gradleScript) {
        repositories = new ArrayList<Repository>();
        repositories.add(new MavenCentralRepository());
        repositories.add(new JCenterRepository());
        repositories.addAll(extractRepositories(gradleScript));
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public List<Repository> extractRepositories(String gradleScript) {
        // TODO not implemented yet
        return new ArrayList<Repository>();
    }
}
