import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * gradleのスクリプトを解析するユーティリティクラス
 */
public class GradleScriptParsingUtils {

    private GradleScriptParsingUtils() {
    }

    /**
     * gradleのスクリプトからMavenのメタデータにアクセスするURLのリストを生成する
     *
     * @param script
     * @return
     */
    public static List<String> createMetaDataUrls(String script) {
        List<String> metaDataUrls = new ArrayList<String>();
        List<String> libraryDeclarationTexts = extractLibraryDeclarationTexts(script);
        for (String libraryDeclarationText : libraryDeclarationTexts) {
            metaDataUrls.add(createMetaDataUrl(libraryDeclarationText));
        }
        return metaDataUrls;
    }

    /**
     * ライブラリ定義のリストからMavenのメタデータにアクセスするURLのリストを生成する
     *
     * @param libraryDeclarationTexts
     * @return
     */
    public static List<String> createMetaDataUrls(List<String> libraryDeclarationTexts) {
        List<String> metaDataUrls = new ArrayList<String>();
        for (String libraryDeclarationText : libraryDeclarationTexts) {
            metaDataUrls.add(createMetaDataUrl(libraryDeclarationText));
        }
        return metaDataUrls;
    }

    /**
     * ライブラリ定義の文字列からMavenのメタデータにアクセスするURLを生成する
     *
     * @param libraryDeclarationText
     * @return
     */
    public static String createMetaDataUrl(String libraryDeclarationText) {
        String path = libraryDeclarationText.replace(".", "/").replace(":", "/");
        return new StringBuilder("http://repo1.maven.org/maven2/")
                .append(path)
                .append("/maven-metadata.xml")
                .toString();
    }

    /**
     * gradleのスクリプトからライブラリ定義の文字列リストを抽出する
     *
     * @param script
     * @return
     */
    public static List<String> extractLibraryDeclarationTexts(String script) {
        String text = new String(script);
        Pattern p = Pattern.compile("(?:compile|provided|debugCompile|releaseCompile|testCompile) +'(.*):");
        Matcher m = p.matcher(text);
        List<String> matchStrings = new ArrayList<String>();

        while (m.find()) {
            matchStrings.add(m.group(1));
            text = text.substring(m.end());
            m = p.matcher(text);
        }

        return matchStrings;
    }
}
