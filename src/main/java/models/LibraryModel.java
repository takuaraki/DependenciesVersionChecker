package models;

import entity.Library;
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

    public Observable<GetLatestLibrariesResult> getLatestLibraries() {
        return Observable.create(new Observable.OnSubscribe<GetLatestLibrariesResult>() {
            @Override
            public void call(Subscriber<? super GetLatestLibrariesResult> subscriber) {
                List<LatestLibrary> getResults = new ArrayList<LatestLibrary>();

                for (int i = 0; i < libraries.size(); i++) {
                    try {
                        String latestVersion = getLatestVersion(libraries.get(i));
                        getResults.add(new LatestLibrary(libraries.get(i), latestVersion));
                    } catch (Exception e) {
                        getResults.add(new LatestLibrary(libraries.get(i), "Not Found"));
                    }
                    subscriber.onNext(new GetLatestLibrariesResult(null, "<b>Getting latest versions (" + (i+1) + "/" + libraries.size() + ")</b>"));
                }

                subscriber.onNext(new GetLatestLibrariesResult(getResults, null));
            }
        });

    }

    private String getLatestVersion(Library library) throws Exception {
        XmlParser xmlParser = new XmlParser();
        Node node = xmlParser.parse(library.getMetaDataUrl());
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

    public static class LatestLibrary extends Library {
        String version;

        public LatestLibrary(Library library, String version) {
            super(library.getGroupId(), library.getArtifactId(), library.getCurrentUsingVersion());
            this.version = version;
        }

        public String getVersion() {
            return version;
        }
    }

    public static class GetLatestLibrariesResult {
        List<LatestLibrary> latestLibraries;
        String progress;

        public GetLatestLibrariesResult(List<LatestLibrary> latestLibraries, String progress) {
            this.latestLibraries = latestLibraries;
            this.progress = progress;
        }

        public List<LatestLibrary> getLatestLibraries() {
            return latestLibraries;
        }

        public String getProgress() {
            return progress;
        }
    }
}
