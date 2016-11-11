package wuxian.me.localbroadcastannotations.compiler;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import wuxian.me.localbroadcastannotations.annotation.OnReceive;

public class LocalBroadcastAnnotationsProcessor extends AbstractProcessor {
    @NonNull
    private Elements elementUtils;
    @NonNull
    private Filer filer;
    @NonNull
    private Messager messager;

    @NonNull
    private final Map<String, AnnotatedMethodsPerClass> mGroupedMethodsMap =
            new LinkedHashMap<>();

    @Override
    public synchronized void init(@NonNull ProcessingEnvironment env) {
        super.init(env);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
    }


    @Override
    public boolean process(@NonNull Set<? extends TypeElement> set,
                           @NonNull RoundEnvironment roundEnv) {
        try {
            processAnnotation(OnReceive.class, roundEnv);
        } catch (ProcessingException e) {
            error(e.getElement(), e.getMessage());
        }

        try {
            warn(null, "Preparing to create %d generated classes.", mGroupedMethodsMap.size());
            AnnotationsFileBuilder.generateFile(mGroupedMethodsMap, elementUtils, filer);
            mGroupedMethodsMap.clear();
        } catch (IOException e) {
            error(null, e.getMessage());
        } catch (ProcessingException e) {
            error(e.getElement(), e.getMessage());
        }

        return true;
    }

    /**
     * collect all annotated method
     *
     * @param annotationClass
     * @param roundEnv
     * @throws ProcessingException
     */
    private void processAnnotation(Class<? extends Annotation> annotationClass, @NonNull RoundEnvironment roundEnv) throws ProcessingException {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotationClass);
        warn(null, "Processing %d elements annotated with @%s", elements.size(), elements);

        for (Element element : elements) {
            if (element.getKind() != ElementKind.METHOD) {
                throw new ProcessingException(element,
                        String.format("Only methods can be annotated with @%s",
                                annotationClass.getSimpleName()));
            } else {
                ExecutableElement executableElement = (ExecutableElement) element;
                try {
                    processMethod(executableElement, annotationClass);
                } catch (IllegalArgumentException e) {
                    throw new ProcessingException(executableElement, e.getMessage());
                }
            }
        }

    }

    private void processMethod(ExecutableElement executableElement, Class<? extends Annotation> annotationClass) throws ProcessingException {
        AnnotatedMethod annotatedMethod = new AnnotatedMethod(executableElement, annotationClass);
        checkMethodValidity(annotatedMethod);

        TypeElement enclosingClass = findEnclosingClass(annotatedMethod);

        if (enclosingClass == null) {
            throw new ProcessingException(null,
                    String.format("Can not find enclosing class for method %s",
                            annotatedMethod.getExecutableElement().getSimpleName().toString()));
        } else {
            String className = enclosingClass.getQualifiedName().toString();  //将该element存入一个class的map中
            AnnotatedMethodsPerClass groupedMethods = mGroupedMethodsMap.get(className);
            if (groupedMethods == null) {
                groupedMethods = new AnnotatedMethodsPerClass(className);
                mGroupedMethodsMap.put(className, groupedMethods);
            }
            groupedMethods.add(annotatedMethod);
        }

    }

    @Nullable
    private TypeElement findEnclosingClass(@NonNull AnnotatedMethod annotatedMethod) {
        TypeElement enclosingClass;

        ExecutableElement methodElement = annotatedMethod.getExecutableElement(); //这里element是一个method的element
        while (true) {
            Element enclosingElement = methodElement.getEnclosingElement();
            if (enclosingElement.getKind() == ElementKind.CLASS) {  //What if an interface???
                enclosingClass = (TypeElement) enclosingElement;
                break;
            }
        }

        return enclosingClass;
    }

    /**
     * 合法性校验
     */
    private void checkMethodValidity(@NonNull AnnotatedMethod item) throws ProcessingException {
        ExecutableElement methodElement = item.getExecutableElement();
        Set<Modifier> modifiers = methodElement.getModifiers();

        // The annotated method needs to be accessible by the generated class which will have
        // the same package. Public or "package private" (default) methods are required.
        if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.PROTECTED)) {
            throw new ProcessingException(methodElement,
                    String.format("The method %s can not be private or protected.",
                            methodElement.getSimpleName().toString()));
        }

        // We cannot annotate abstract methods, we need to annotate the actual implementation of
        // the method on the implementing class.
        if (modifiers.contains(Modifier.ABSTRACT)) {
            throw new ProcessingException(methodElement, String.format(
                    "The method %s is abstract. You can't annotate abstract methods with @%s",
                    methodElement.getSimpleName().toString(), AnnotatedMethod.class.getSimpleName()));
        }
    }


    private void error(@Nullable Element e, @NonNull String msg, @Nullable Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }

    private void warn(@Nullable Element e, @NonNull String msg, @Nullable Object... args) {
        messager.printMessage(Diagnostic.Kind.WARNING, String.format(msg, args), e);
    }
}
