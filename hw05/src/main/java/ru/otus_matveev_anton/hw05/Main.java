package ru.otus_matveev_anton.hw05;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by asfarus on 09.05.17.
 */
public class Main {
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        String packageName = "ru.otus_matveev_anton.hw05";
        getClassesForPackage(packageName).forEach(System.out::println);
    }

    private static List<Class> getClassesForPackage(String pckgname) throws ClassNotFoundException {
        ArrayList<File> directories = new ArrayList<>();
        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }
            String path = pckgname.replace('.', '/');
            // Ask for all resources for the path
            Enumeration<URL> resources = cld.getResources(path);
            while (resources.hasMoreElements()) {
                directories.add(new File(URLDecoder.decode(resources.nextElement().getPath(), "UTF-8")));
            }
        } catch (IOException e) {
            throw new ClassNotFoundException("Пакет не найден " + pckgname);
        }

        ArrayList<Class> classes = new ArrayList<>();
        // For every directory identified capture all the .class files
        for (File directory : directories) {
            if (directory.exists()) {
                // Get the list of the files contained in the package
                String[] files = directory.list();
                for (String file : files) {
                    // we are only interested in .class files
                    if (file.endsWith(".class")) {
                        // removes the .class extension
                        try {
                            classes.add(Class.forName(pckgname + '.' + file.substring(0, file.length() - 6)));
                        } catch (NoClassDefFoundError e) {
                        }
                    }
                }
            } else {
                throw new ClassNotFoundException(pckgname + " (" + directory.getPath() + ") does not appear to be a valid package");
            }
        }
        return classes;
    }
}
