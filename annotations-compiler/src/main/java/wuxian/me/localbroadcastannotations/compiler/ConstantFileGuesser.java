package wuxian.me.localbroadcastannotations.compiler;

import com.squareup.javapoet.ClassName;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.Messager;
import javax.lang.model.element.PackageElement;

/**
 * Created by wuxian on 22/11/2016.
 * Class used to find android constant file
 */

public class ConstantFileGuesser {
    private static final String SETTINGS_GRADLE = "settings.gradle";
    private static final String BUILD_GRADLE = "build.gradle";

    private ConstantFileGuesser() {
    }

    private static ConstantFileGuesser instance;

    public static ConstantFileGuesser getInstance() {
        if (instance == null) {
            instance = new ConstantFileGuesser();
        }

        return instance;
    }

    private List<ClassName> mConstantClassNames;
    private String mPackageName;
    private File mProjectRoot;
    private File mJavaRootDirectory;
    private Messager messager;

    /**
     * Localbroadcast的Intent的Actioin String一般会被写到一个单独的文件名一般包含了"constant"的文件中 需要找出它们
     * 然后通过 JavaFile.Builder.addStaticImport()函数导入到文件中
     */
    public List<ClassName> guess(@NonNull Messager messager, @NonNull PackageElement packageElement) throws ProcessingException {
        if (mConstantClassNames == null) {
            this.messager = messager;
            File cur = new File(new File(".").getAbsolutePath()); // /User/wuxian/Documents/OpenSourceProj/LocalBroadcastAnnotations
            File root = findRootDirectory(cur.getParentFile());
            mProjectRoot = root;
            mJavaRootDirectory = getJavaRootFile(root, packageElement.toString());
            mConstantClassNames = guessConstantClassNames(mJavaRootDirectory);
        }
        return mConstantClassNames;
    }

    /**
     * Should always be rootFile,otherwise just return null
     */
    @Nullable
    private File findRootDirectory(File fromFile) {
        if (fromFile != null && fromFile.isDirectory() && isRootDirectory(fromFile)) {
            return fromFile;
        }
        return null;
    }

    /**
     * Currently only support Gradle project! not maven!
     */
    private boolean isRootDirectory(File dir) {
        boolean settingFileExsit = false;
        boolean buildFileExsit = false;

        File[] files;
        files = dir.listFiles();

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.getName().equals(SETTINGS_GRADLE)) {
                settingFileExsit = true;
            } else if (file.getName().equals(BUILD_GRADLE)) {
                buildFileExsit = true;
            }

            if (settingFileExsit && buildFileExsit) {
                break;
            }
        }
        return (settingFileExsit && buildFileExsit);
    }

    /**
     * get javarootfile directory:for eg,your-proj/app/src/main/java
     * 先从settings.gradle里面读取module name(一个project可能有多个module,目前只支持android application不支持library),
     * 然后找到存在@packagename的那一个module
     */
    private File getJavaRootFile(File rootDir, String packageName) {
        if (rootDir == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder("");

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(rootDir.getAbsolutePath() + "/" + SETTINGS_GRADLE)));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (FileNotFoundException e) {
            LocalBroadcastAnnotationsProcessor.error(messager, null, String.format("settings.gradle not find"));
        } catch (IOException e) {
            LocalBroadcastAnnotationsProcessor.error(messager, null, String.format("reading settings.gradle IOException"));
        } catch (Exception e) {
            LocalBroadcastAnnotationsProcessor.error(messager, null, String.format("exception"));
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    LocalBroadcastAnnotationsProcessor.error(messager, null, String.format("close settings.gradle IOException"));
                }

            }
        }

        String content = builder.toString();
        String pattern = "(?<=['\"]:)[-\\w]+(?=['\"])";
        List<String> modules = new ArrayList<>();
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(content);
        while (m.find()) {
            modules.add(m.group());
        }

        return findValidJavaRoot(modules, packageName);
    }

    private File findValidJavaRoot(List<String> modules, String packageName) {
        if (mProjectRoot == null || modules == null || modules.size() == 0 || packageName == null || packageName.length() == 0) {
            return null;
        }
        Pattern p = Pattern.compile("\\.");
        Matcher m = p.matcher(packageName);
        String packagePath = m.replaceAll("/");  //将wuxian.me.demo替换为wuxian/me/demo

        for (String module : modules) {
            if (new File((mProjectRoot + "/" + module + "/src/main/java/" + packagePath)).exists()) {
                return new File(mProjectRoot + "/" + module + "/src/main/java/");
            }
        }

        return null;
    }


    private List<ClassName> guessConstantClassNames(File javaRootDir) {
        List<ClassName> classNames = new ArrayList<>();
        if (javaRootDir == null || !javaRootDir.isDirectory()) {
            return classNames;
        }

        Queue<File> queue = new LinkedBlockingQueue<>();
        queue.add(javaRootDir);

        Pattern p = Pattern.compile("constant.*\\.java", Pattern.CASE_INSENSITIVE);   //类似xxxconstantxxx.java文件

        List<File> validFiles = new ArrayList<>();

        while (!queue.isEmpty()) {
            File[] files = queue.poll().listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    queue.add(file);
                    continue;
                }

                Matcher m = p.matcher(file.getName());
                if (m.find()) {
                    validFiles.add(file);
                    continue;
                }
            }
        }

        Pattern pattern = Pattern.compile("(?<=/java/).*");
        for (File file : validFiles) {
            Matcher m = pattern.matcher(file.getParentFile().getAbsolutePath());
            if (!m.find()) {
                continue;
            }
            String packagePath = m.group();

            Pattern pattern1 = Pattern.compile("/");
            Matcher m2 = pattern1.matcher(packagePath);
            String packageName = m2.replaceAll("\\.");

            Pattern pattern2 = Pattern.compile(".*(?=\\.java)");
            Matcher m3 = pattern2.matcher(file.getName());
            if (m3.find()) {
                String className = m3.group();
                ClassName cn = ClassName.get(packageName, className);
                classNames.add(cn);
            }
        }

        return classNames;
    }
}
