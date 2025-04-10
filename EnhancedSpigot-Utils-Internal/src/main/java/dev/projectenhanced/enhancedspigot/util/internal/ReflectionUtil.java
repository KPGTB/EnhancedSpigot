package dev.projectenhanced.enhancedspigot.util.internal;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Util to get classes from package
 */
public class ReflectionUtil {
    /**
     * This method returns all classes in specified package
     * @param jarFile JarFile in which is this plugin
     * @param packageName Name of package that have this classes
     * @return Set of classes in package
     */
    public static Set<Class<?>> getAllClassesInPackage(File jarFile, String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        try {
            JarFile file = new JarFile(jarFile);

            for (Enumeration<JarEntry> entry = file.entries(); entry.hasMoreElements();) {
                JarEntry jarEntry = entry.nextElement();
                String name = jarEntry.getName().replace("/", ".");
                if(name.startsWith(packageName) && name.endsWith(".class"))
                    classes.add(Class.forName(name.substring(0, name.length() - 6)));
            }
            file.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * This method returns all classes that extends specified class in specified package
     * @param jarfile JarFile in which is this plugin
     * @param packageName Name of package that have this classes
     * @param abstractClass Class that need to be extended
     * @return Set of classes in package
     */
    public static Set<Class<?>> getAllClassesInPackage(File jarfile, String packageName, Class<?> abstractClass) {
        Set<Class<?>> classes = getAllClassesInPackage(jarfile, packageName);
        return classes.stream()
                .filter(clazz -> clazz.getSuperclass().equals(abstractClass))
                .collect(Collectors.toSet());
    }
}