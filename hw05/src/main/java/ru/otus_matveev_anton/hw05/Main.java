package ru.otus_matveev_anton.hw05;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Created by asfarus on 09.05.17.
 */
public class Main {
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        String packageName = "ru.otus_matveev_anton.hw05";

        Stream.of(getClassesForPackage(packageName)).forEach(System.out::println);
    }

    private static Class[] getClassesForPackage(String packageName) throws ClassNotFoundException, IOException {

        ClassLoader cld = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        Enumeration<URL> resources = cld.getResources(packagePath);
        if (resources.hasMoreElements()) {
            String path = URLDecoder.decode(resources.nextElement().getPath(), "UTF-8");
            if (path != null) {
                Set<Class> classes = new HashSet<>();
                if (path.startsWith("file:/")) {
                    String jarName = path.split("file:/", 2)[1].split("!/", 2)[0];
                    try (JarFile jarFile = new JarFile(jarName)) {
                        Enumeration<JarEntry> enumeration = jarFile.entries();
                        String filePath;
                        while (enumeration.hasMoreElements()) {
                            filePath = enumeration.nextElement().getName();
                            if (filePath.startsWith(packagePath) && filePath.endsWith(".class")) {
                                classes.add(Class.forName(getClassNameByFilePath(filePath)));
                            }
                        }
                    }
                } else {
                    Path dir = new File(path).toPath();
                    if (Files.exists(dir)) {
                        final Set<String> classFileNames = new HashSet<>();

                        Files.walkFileTree(dir,
                                new SimpleFileVisitor<Path>() {
                                    StringBuilder sb = new StringBuilder();
                                    int lvl = 0;
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
                            classes.add(Class.forName(packageName + "." + getClassNameByFilePath(classFile)));
                        }
                    }
                }
                 classes.toArray(new Class[classes.size()]);
            }
        }
        throw new ClassNotFoundException("Not found package " + packageName);
    }

    private static String getClassNameByFilePath(String filePath){
        return filePath.replaceAll("(\\.class|\\$[0-9]+)", "").replace('/', '.');
    }

}
