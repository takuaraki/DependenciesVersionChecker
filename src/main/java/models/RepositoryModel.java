package models;

import entities.repositories.JCenterRepository;
import entities.repositories.MavenCentralRepository;
import entities.repositories.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * extract Repositories from gradle script.
     *
     * @param gradleScript
     * @return
     */
    private List<Repository> extractRepositories(String gradleScript) {
        String text = new String(gradleScript);
        String regex = "maven(?: |\\n|\\t)*\\{(?: |\\n|\\t)*url(?: |\\n|\\t)*(?:'|\")(.*)(?:'|\")";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        List<Repository> repositories = new ArrayList<Repository>();

        while (m.find()) {
            String url = m.group(1);
            if (url.substring(url.length() - 1, url.length()).equals("/") == false) {
                url += "/";
            }
            Repository repository = new Repository(url);
            repositories.add(repository);

            text = text.substring(m.end());
            m = p.matcher(text);
        }

        return repositories;
    }
}
