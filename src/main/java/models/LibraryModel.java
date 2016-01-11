package models;

import entity.Library;
import groovy.util.Node;
import groovy.util.XmlParser;
import groovy.xml.QName;
import rx.Observable;
import rx.Subscriber;
import rx.functions.FuncN;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model of library available from maven repository.
 */
public class LibraryModel {

    List<Library> libraries;

    public LibraryModel() {
        libraries = new ArrayList<Library>();
    }

    /**
     * init LibraryModel
     *
     * @param gradleScript
     */
    public void init(String gradleScript) {
        libraries = extractLibraries(gradleScript);

        for (int i = 0; i < libraries.size(); i++) {
            libraries.get(i).setMetaDataUrl(createMetaDataUrl(libraries.get(i)));
        }
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    public Observable<List<GetLatestLibraryResult>> getLatestLibraries() {
        List<Observable<GetLatestLibraryResult>> observables = new ArrayList<Observable<GetLatestLibraryResult>>();
        for (Library library : libraries) {
            observables.add(getLatestLibrary(library));
        }

        return Observable.zip(observables, new FuncN<List<GetLatestLibraryResult>>() {
            @Override
            public List<GetLatestLibraryResult> call(Object... args) {
                List<GetLatestLibraryResult> latestVersions = new ArrayList<GetLatestLibraryResult>();
                for (Object arg : args) {
                    GetLatestLibraryResult latestVersion = (GetLatestLibraryResult) arg;
                    latestVersions.add(latestVersion);
                }
                return latestVersions;
            }
        });
    }

    public Observable<GetLatestLibraryResult> getLatestLibrary(final Library library) {
        return Observable.create(new Observable.OnSubscribe<GetLatestLibraryResult>() {
            @Override
            public void call(Subscriber<? super GetLatestLibraryResult> subscriber) {
                try {
                    XmlParser xmlParser = new XmlParser();
                    Node node = xmlParser.parse(library.getMetaDataUrl());
                    String latestVersion = node.getAt(QName.valueOf("versioning")).getAt("latest").text();
                    subscriber.onNext(new GetLatestLibraryResult(library, latestVersion));
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onNext(new GetLatestLibraryResult(library, "Not Found"));
                    subscriber.onCompleted();
                }
            }
        });
    }

    /**
     * extract Libraries from gradle script.
     *
     * @param gradleScript
     * @return
     */
    private List<Library> extractLibraries(final String gradleScript) {
        String text = new String(gradleScript);
        Pattern p = Pattern.compile("(?:compile|provided|debugCompile|releaseCompile|testCompile) +'(.*):(.*):(.*)'");
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

    /**
     * create URL to access meta data of library
     *
     * @param library
     * @return
     */
    private String createMetaDataUrl(Library library) {
        return new StringBuilder("http://repo1.maven.org/maven2/")
                .append(library.getGroupId().replace(".", "/"))
                .append("/")
                .append(library.getArtifactId())
                .append("/maven-metadata.xml")
                .toString();
    }

    public static class GetLatestLibraryResult {
        Library library;
        String latestVersion;

        public GetLatestLibraryResult(Library library, String latestVersion) {
            this.library = library;
            this.latestVersion = latestVersion;
        }

        public Library getLibrary() {
            return library;
        }

        public String getLatestVersion() {
            return latestVersion;
        }
    }
}
