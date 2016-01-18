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

        for (int i = 0; i < usingLibraries.size(); i++) {
            usingLibraries.get(i).setMetaDataUrl(createMetaDataUrl(usingLibraries.get(i)));
        }
    }

    public List<Library> getUsingLibraries() {
        return usingLibraries;
    }

    public Observable<GetLatestLibrariesResult> getLatestLibraries() {
        return Observable.create(new Observable.OnSubscribe<GetLatestLibrariesResult>() {
            @Override
            public void call(Subscriber<? super GetLatestLibrariesResult> subscriber) {
                List<Library> latestLibraries = new ArrayList<Library>();

                for (int i = 0; i < usingLibraries.size(); i++) {
                    String latestVersion;
                    try {
                        latestVersion = getLatestVersion(usingLibraries.get(i));
                    } catch (Exception e) {
                        latestVersion = "Not Found";
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
        Pattern p = Pattern.compile("(?:compile|provided|debugCompile|releaseCompile|testCompile)(?: *| *\\()(?:'|\")(.*):(.*):(.*)(?:'|\")");
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
