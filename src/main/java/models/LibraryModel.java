package models;

import com.fasterxml.jackson.databind.ObjectMapper;
import dtos.Dependency;
import dtos.DependencyUpdatesResult;
import entities.Library;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import rx.Observable;
import rx.Subscriber;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Model of library
 */
public class LibraryModel {

    String basePath;


    public void init(String basePath) {
        this.basePath = basePath;
    }

    public Observable<List<Library>> getLibraries(final String gradleScript) {
        return Observable.create(new Observable.OnSubscribe<List<Library>>() {
            @Override
            public void call(final Subscriber<? super List<Library>> subscriber) {
                makeReWrittenScriptFile(gradleScript);

                ProjectConnection connection = GradleConnector.newConnector()
                        .forProjectDirectory(new File(basePath + "/build/DependenciesVersionChecker"))
                        .connect();

                connection.newBuild().forTasks("dependencyUpdates").withArguments("-DoutputFormatter=json").run(new ResultHandler<Void>() {
                    @Override
                    public void onComplete(Void aVoid) {
                        List<Library> libraries = new ArrayList<Library>();

                        DependencyUpdatesResult result = getDependencyUpdatesResult();
                        for (Dependency dependency : result.getCurrentDependencies()) {
                            libraries.add(Library.create(dependency, Library.Status.CURRENT));
                        }
                        for (Dependency dependency : result.getExceededDependencies()) {
                            libraries.add(Library.create(dependency, Library.Status.EXCEED));
                        }
                        for (Dependency dependency : result.getOutdatedDependencies()) {
                            libraries.add(Library.create(dependency, Library.Status.OUTDATED));
                        }

                        subscriber.onNext(libraries);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onFailure(GradleConnectionException e) {
                        subscriber.onError(e);
                    }
                });
            }
        });
    }

    private void makeReWrittenScriptFile(String gradleScript) {
        new File(basePath + "/build").mkdir();
        new File(basePath + "/build/DependenciesVersionChecker").mkdir();
        File file = new File(basePath + "/build/DependenciesVersionChecker/build.gradle");
        file.delete();

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            pw.println(
                    "buildscript {\n" +
                            "    repositories {\n" +
                            "        jcenter()\n" +
                            "    }\n" +
                            "    dependencies {\n" +
                            "        classpath 'com.github.ben-manes:gradle-versions-plugin:0.12.0'\n" +
                            "    }\n" +
                            "}\n" +
                            "apply plugin: 'com.github.ben-manes.versions'");
            pw.print(gradleScript);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pw.close();
        }
    }

    private DependencyUpdatesResult getDependencyUpdatesResult() {
        DependencyUpdatesResult result = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonPath = basePath + "/build/DependenciesVersionChecker/build/dependencyUpdates/report.json";
            result = mapper.readValue(new File(jsonPath), DependencyUpdatesResult.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
