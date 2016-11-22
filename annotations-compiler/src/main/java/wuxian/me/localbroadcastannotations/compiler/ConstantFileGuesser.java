package wuxian.me.localbroadcastannotations.compiler;

import com.squareup.javapoet.ClassName;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.PackageElement;

/**
 * Created by wuxian on 22/11/2016.
 * Class used to find android constant file
 * TODO:
 */

public class ConstantFileGuesser {
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
    private File mRootDirectory;

    /**
     * Localbroadcast的Intent的Actioin String一般会被写到一个单独的文件名一般包含了"constant"的文件中 需要找出它们
     * 然后通过 JavaFile.Builder.addStaticImport()函数导入到文件中
     */
    public List<ClassName> guess(@NonNull Messager messager, @NonNull PackageElement packageElement) {
        if (mConstantClassNames == null) {
            Messager sMessager = messager;
            LocalBroadcastAnnotationsProcessor.info(sMessager, null, "packageElement is " + packageElement.toString());
            //wuxian.me.localbroadcastdemo
            File cur = new File(new File(".").getAbsolutePath());
            try {
                LocalBroadcastAnnotationsProcessor.info(sMessager, null, "current directory is " + cur.getCanonicalPath());
                // /User/wuxian/Documents/OpenSourceProj/LocalBroadcastAnnotations
            } catch (Exception e) {
                ;
            }

            mRootDirectory = findRootDirectory(cur, packageElement.toString());
            mPackageName = getPackageNameFromRootFile(mRootDirectory);

            mConstantClassNames = guessConstantClassNames(mRootDirectory, mPackageName);
        }
        return mConstantClassNames;
    }

    /**
     * TODO:
     */
    private File findRootDirectory(File fromFile, String fromPackage) {
        return fromFile;
    }

    /**
     * TODO:
     */
    private String getPackageNameFromRootFile(File rootDir) {
        return "";
    }

    /**
     * TODO:
     */
    private List<ClassName> guessConstantClassNames(File rootDir, String packageName) {
        return null;
    }


}
