import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import entities.DependencyUpdatesResult;
import entities.Library;
import models.LibraryModel;
import org.apache.http.util.TextUtils;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.jetbrains.annotations.NotNull;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.schedulers.SwingScheduler;
import viewmodels.VersionCheckViewModel;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.*;
import java.net.URISyntaxException;
import java.util.List;

/**
 * バーションチェックを行うToolWindow
 */
public class VersionCheckWindow implements ToolWindowFactory {

    private static final String INPUT_AREA_HINT = "Input your 'build.gradle' script.";
    private static final String ERROR_MESSAGE_NO_LIBRARY = "<span style=\"color:red\">[Error] No library declaration is found. Please input gradle script which contains dependencies blocks.</span>";

    private JPanel toowWindowContent;
    private JButton versionCheckButton;
    private JTextArea inputArea;
    private JEditorPane resultArea;

    private Project currentProject;

    VersionCheckViewModel versionCheckViewModel;


    public VersionCheckWindow() {
        versionCheckViewModel = new VersionCheckViewModel();

        inputArea.setText(INPUT_AREA_HINT);
        inputArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                if (inputArea.getText().equals(INPUT_AREA_HINT)) {
                    inputArea.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                if (inputArea.getText().equals("")) {
                    inputArea.setText(INPUT_AREA_HINT);
                }
            }
        });

        versionCheckButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                makeReWrittenScriptFile(inputArea.getText());

                ProjectConnection connection = GradleConnector.newConnector()
                        .forProjectDirectory(new File(currentProject.getBasePath() + "/build/DependenciesVersionChecker"))
                        .connect();
                connection.newBuild().forTasks("dependencyUpdates").withArguments("-DoutputFormatter=json").run(new ResultHandler<Void>() {
                    @Override
                    public void onComplete(Void aVoid) {
                        parseDependencyUpdatesResult();
                    }

                    @Override
                    public void onFailure(GradleConnectionException e) {

                    }
                });
            }
        });

        resultArea.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            protected void hyperlinkActivated(HyperlinkEvent hyperlinkEvent) {
                if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(hyperlinkEvent.getURL().toURI());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void parseDependencyUpdatesResult() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonPath = currentProject.getBasePath() + "/build/DependenciesVersionChecker/build/dependencyUpdates/report.json";
            DependencyUpdatesResult result = mapper.readValue(new File(jsonPath), DependencyUpdatesResult.class);
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeReWrittenScriptFile(String gradleScript) {
        new File(currentProject.getBasePath() + "/build").mkdir();
        new File(currentProject.getBasePath() + "/build/DependenciesVersionChecker").mkdir();
        File file = new File(currentProject.getBasePath() + "/build/DependenciesVersionChecker/build.gradle");
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

    private String createResult(List<Library> usingLibraries, List<Library> latestLibraries) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<table>")
                .append("<tr><th align=\"left\">Library</th><th align=\"left\">Using version</th><th align=\"left\">Latest version</th></tr>");
        for (int i = 0; i < usingLibraries.size(); i++) {
            Library usingLibrary = usingLibraries.get(i);

            String extractedLibrary;
            if (TextUtils.isEmpty(usingLibrary.getMetaDataUrl())) {
                extractedLibrary = usingLibrary.getGroupId() + ":" + usingLibrary.getArtifactId();
            } else {
                extractedLibrary =
                        "<a href=\"" + usingLibrary.getMetaDataUrl() + "\">"
                                + usingLibrary.getGroupId() + ":" + usingLibrary.getArtifactId() + "</a>";
            }

            stringBuilder
                    .append("<tr>")
                    .append("<td>").append(extractedLibrary).append("</td>")
                    .append("<td>").append(usingLibrary.getVersion()).append("</td>")
                    .append("<td>").append(latestLibraries.get(i).getVersion()).append("</td>")
                    .append("</tr>");
        }
        stringBuilder.append("</table>");
        return stringBuilder.toString();
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        currentProject = project;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(toowWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
