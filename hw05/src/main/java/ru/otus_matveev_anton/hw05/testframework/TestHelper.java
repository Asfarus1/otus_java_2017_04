package ru.otus_matveev_anton.hw05.testframework;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class TestHelper {
    protected TestHelper() {}

    public static void executeTests(Class... classes) throws Exception {
        List<Method> beforeMethods = new ArrayList<>();
        List<Method> testMethods = new ArrayList<>();
        List<Method> afterMethods = new ArrayList<>();
        Object obj;
        int ok = 0, failed = 0, all = 0, skipped = 0;
        System.out.println("TESTS");
        try {
            for (Class aClass : classes) {
                for (Method method : aClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Before.class)) {
                        beforeMethods.add(method);
                    }
                    if (method.isAnnotationPresent(Test.class)) {
                        testMethods.add(method);
                    }
                    if (method.isAnnotationPresent(After.class)) {
                        afterMethods.add(method);
                    }
                }
                obj = aClass.newInstance();

                for (Method testMethod : testMethods) {
                    if (testMethod.isAnnotationPresent(Ignore.class)) {
                        skipped++;
                    } else {
                        for (Method beforeMethod : beforeMethods) {
                            beforeMethod.invoke(obj);
                        }
                        try {
                            testMethod.invoke(obj);
                            ok++;
                            System.out.printf("test %s.%s - Ok%n", aClass.getCanonicalName(), testMethod.getName());
                        } catch (InvocationTargetException e) {
                            failed++;
                            System.out.printf("test %s.%s - failed", aClass.getCanonicalName(), testMethod.getName());
                            Throwable cause = e.getCause();
                            if (cause==null){
                                System.out.println();
                            }else {
                                System.out.printf(" : %s%n", cause.getMessage());
                            }
                            e.printStackTrace(System.out);
                        }
                        for (Method afterMethod : afterMethods) {
                            afterMethod.invoke(obj);
                        }
                    }
                    all++;
                }
            }
            System.out.printf("All test count %d, %d - Ok, %d - skipped, %d - failed%n", all, ok, skipped, failed);
        }catch (Exception e){
            System.out.println("TESTS FAILED");
            e.printStackTrace(System.out);
        }
    }

    public static void executeTestsInPackage(String packageName) throws Exception{

        executeTests(getClassesForPackage(packageName, (c) ->
                Stream.of(c.getDeclaredMethods()).anyMatch(method -> method.getDeclaredAnnotation(Test.class) != null)
        ));
    }

    private static Class[] getClassesForPackage(String packageName, Predicate<Class> filter) throws ClassNotFoundException, IOException {
        Objects.requireNonNull(filter);

        ClassLoader cld = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        Enumeration<URL> resources = cld.getResources(packagePath);

        if (resources.hasMoreElements()) {
            String path = URLDecoder.decode(resources.nextElement().getPath(), "UTF-8");
            Class clazz;
            if (path != null) {
                Set<Class> classes = new HashSet<>();

                //для поиска в jar
                if (path.startsWith("file:/")) {
                    String jarName = path.split("file:/", 2)[1].split("!/", 2)[0];
                    try (JarFile jarFile = new JarFile(jarName)) {
                        Enumeration<JarEntry> enumeration = jarFile.entries();
                        String filePath;
                        while (enumeration.hasMoreElements()) {
                            filePath = enumeration.nextElement().getName();
                            if (filePath.startsWith(packagePath) && filePath.endsWith(".class")) {
                                clazz = Class.forName(getClassNameByFilePath(filePath));
                                if (filter.test(clazz)) {
                                    classes.add(clazz);
                                }
                            }
                        }
                    }
                } else {
                    //для поиска в кателоге
                    Path dir = new File(path).toPath();
                    if (Files.exists(dir)) {
                        final Set<String> classFileNames = new HashSet<>();

                        Files.walkFileTree(dir,
                                new SimpleFileVisitor<Path>() {
                                    StringBuilder sb = new StringBuilder();
                                    int lvl;

                                    @Override
                                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                                        if (lvl++>0) {
                                            sb.append(".").append(dir.getFileName());
                                        }
                                        return FileVisitResult.CONTINUE;
                                    }

                                    @Override
                                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                        if (lvl-->1) {
                                            sb.setLength(sb.length() - 1 - dir.getFileName().toString().length());
                                        }
                                        return FileVisitResult.CONTINUE;
                                    }

                                    @Override
                                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                        String fileName = file.getFileName().toString();
                                        if (fileName.endsWith(".class")) {
                                            if (lvl>1){
                                                classFileNames.add(getClassNameByFilePath(sb.substring(1).concat(".").concat(fileName)));
                                            }else {
                                                classFileNames.add(getClassNameByFilePath(fileName));
                                            }
                                        }
                                        return FileVisitResult.CONTINUE;
                                    }
                                }
                        );
                        for (String classFile :  classFileNames) {
                            clazz = Class.forName(packageName + "." + getClassNameByFilePath(classFile));
                            if (filter.test(clazz)) {
                                classes.add(clazz);
                            }
                        }
                    }
                }
                 return classes.toArray(new Class[classes.size()]);
            }
        }
        throw new ClassNotFoundException("Not found package " + packageName);
    }

    private static String getClassNameByFilePath(String filePath){
        return filePath.replaceAll("(\\.class|\\$[0-9]+)", "").replace('/', '.');
    }
}
