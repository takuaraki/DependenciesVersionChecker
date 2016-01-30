package models;

import entities.Library;
import entities.repositories.Repository;
import groovy.util.Node;
import groovy.util.XmlParser;
import groovy.xml.QName;
import rx.Observable;
import rx.Subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model of library available from maven repository.
 */
public class LibraryModel {

    List<Library> usingLibraries;

    public LibraryModel() {
        usingLibraries = new ArrayList<Library>();
    }

    /**
     * init LibraryModel
     *
     * @param gradleScript
     */
    public void init(String gradleScript) {
        usingLibraries = extractLibraries(gradleScript);
    }

    public List<Library> getUsingLibraries() {
        return usingLibraries;
    }

    public Observable<GetLatestLibrariesResult> getLatestLibraries(final List<Repository> repositories) {
        return Observable.create(new Observable.OnSubscribe<GetLatestLibrariesResult>() {
            @Override
            public void call(Subscriber<? super GetLatestLibrariesResult> subscriber) {
                List<Library> latestLibraries = new ArrayList<Library>();

                for (int i = 0; i < usingLibraries.size(); i++) {
                    String latestVersion = "Not Found";

                    for (Repository repository : repositories) {
                        try {
                            latestVersion = getLatestVersion(usingLibraries.get(i), repository);
                            usingLibraries.get(i).setMetaDataUrl(repository.getUrl());
                            break;
                        } catch (Exception e) {
                            // meta data not found
                        }
                    }

                    Library latestLibrary = new Library(usingLibraries.get(i));
                    latestLibrary.setVersion(latestVersion);
                    latestLibraries.add(latestLibrary);
                    subscriber.onNext(new GetLatestLibrariesResult(null, usingLibraries,"<b>Getting latest versions (" + (i+1) + "/" + usingLibraries.size() + ")</b>"));
                }

                subscriber.onNext(new GetLatestLibrariesResult(latestLibraries, usingLibraries, null));
            }
        });

    }

    private String getLatestVersion(Library library, Repository repository) throws Exception {
        XmlParser xmlParser = new XmlParser();
        Node node = xmlParser.parse(repository.getUrl() + library.getMetaDataPath());
        String latestVersion = node.getAt(QName.valueOf("versioning")).getAt("latest").text();
        return latestVersion;
    }

    /**
     * extract Libraries from gradle script.
     *
     * @param gradleScript
     * @return
     */
    private List<Library> extractLibraries(final String gradleScript) {
        List<Library> libraries = new ArrayList<Library>();

        // ex) compile compile 'io.reactivex:rxjava:1.1.0'
        String stringNotationRegex = "(?:compile|provided|debugCompile|releaseCompile|testCompile)(?: *| *\\()(?:'|\")(.*):(.*):(.*)(?:'|\")";
        libraries.addAll(extractLibraries(gradleScript, stringNotationRegex));

        // ex) compile group 'io.reactivex', name 'rxjava' version '1.1.0'
        String mapStyleNotationRegex = "(?:compile|provided|debugCompile|releaseCompile|testCompile)(?: +| *\\()group *: *(?:'|\")(.*)(?:'|\") *, *name *: *(?:'|\")(.*)(?:'|\"), *version *: *(?:'|\")(.*)(?:'|\")";
        libraries.addAll(extractLibraries(gradleScript, mapStyleNotationRegex));

        return libraries;
    }

    private List<Library> extractLibraries(final String gradleScript, String regex) {
        String text = new String(gradleScript);
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        List<Library> libraries = new ArrayList<Library>();

        while (m.find()) {
            String groupId = m.group(1);
            String artifactId = m.group(2);
            String currentUsingVersion = m.group(3);
            Library library = new Library(groupId, artifactId, currentUsingVersion);
            libraries.add(library);

            text = text.substring(m.end());
            m = p.matcher(text);
        }

        return libraries;
    }

    public static class GetLatestLibrariesResult {
        List<Library> latestLibraries;
        List<Library> usingLibraries;
        String progress;

        public GetLatestLibrariesResult(List<Library> latestLibraries, List<Library> usingLibraries, String progress) {
            this.latestLibraries = latestLibraries;
            this.usingLibraries = usingLibraries;
            this.progress = progress;
        }

        public List<Library> getLatestLibraries() {
            return latestLibraries;
        }

        public List<Library> getUsingLibraries() {
            return usingLibraries;
        }

        public String getProgress() {
            return progress;
        }
    }
}
