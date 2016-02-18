import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import entities.Library;
import org.gradle.tooling.GradleConnectionException;
import org.jetbrains.annotations.NotNull;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.schedulers.SwingScheduler;
import viewmodels.VersionCheckViewModel;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * バーションチェックを行うToolWindow
 */
public class VersionCheckWindow implements ToolWindowFactory {

    private static final String INPUT_AREA_HINT = "Input your 'build.gradle' script.";

    private JPanel toowWindowContent;
    private JButton versionCheckButton;
    private JTextArea inputArea;
    private JEditorPane resultArea;

    private VersionCheckViewModel versionCheckViewModel;


    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        versionCheckViewModel.init(project.getBasePath());

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(toowWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);
    }

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
                versionCheckViewModel
                        .getLibraries(inputArea.getText())
                        .subscribeOn(Schedulers.io())
                        .observeOn(SwingScheduler.getInstance())
                        .subscribe(new Action1<java.util.List<Library>>() {
                            @Override
                            public void call(List<Library> libraries) {
                                resultArea.setText(createResult(libraries));

                                Notifications.Bus.notify(new Notification("versionCheckFinish", "Dependencies Version Checker", "Version check finished.", NotificationType.INFORMATION));
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                if (throwable instanceof IOException) {
                                    Notifications.Bus.notify(new Notification("versionCheckError", "Dependencies Version Checker", "I/O Error.", NotificationType.ERROR));
                                } else if (throwable instanceof GradleConnectionException) {
                                    Notifications.Bus.notify(new Notification("versionCheckError", "Dependencies Version Checker", "Error occurred when processing inputted script by gradle.", NotificationType.ERROR));
                                }

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

    private String createResult(List<Library> libraries) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<table>")
                .append("<tr><th align=\"left\">Library</th><th align=\"left\">Using version</th><th align=\"left\">Latest version</th></tr>");

        for (Library library : libraries) {
            String title = library.getGroupId() + ":" + library.getArtifactId();
            stringBuilder
                    .append("<tr>")
                    .append("<td>").append(title).append("</td>")
                    .append("<td>").append(library.getCurrentVersion()).append("</td>")
                    .append("<td>").append(library.getLatestVersion()).append("</td>")
                    .append("</tr>");
        }

        stringBuilder.append("</table>");
        return stringBuilder.toString();
    }
}
