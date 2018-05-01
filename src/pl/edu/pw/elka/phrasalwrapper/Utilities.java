package pl.edu.pw.elka.phrasalwrapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Utilities {

    public static Path getResourcePath(String pathToResourceInsideJar) throws IOException {
        String pathToJar = Utilities.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPathToJar = URLDecoder.decode(pathToJar, "UTF-8");

        URI uri = URI.create("jar:file:"+decodedPathToJar);
        Map<String, String> env = new HashMap<>();
        FileSystem fs = FileSystems.newFileSystem(uri, env);
        return fs.getPath(pathToResourceInsideJar);
    }

    public static void extractDirectory(String folderPath, String outputPath) throws IOException {
        String directoryName = folderPath.startsWith("/") ? folderPath.substring(1, folderPath.length()) : folderPath;

        String pathToJar = Utilities.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPathToJar = URLDecoder.decode(pathToJar, "UTF-8");
        JarFile jarFile = new JarFile(decodedPathToJar);

        URI uriToJar = URI.create("jar:file:"+decodedPathToJar);
        Map<String, String> env = new HashMap<>();
        FileSystem fs = FileSystems.newFileSystem(uriToJar, env);

        JarEntry entry;
        for (Enumeration<JarEntry> enumEntry = jarFile.entries(); enumEntry.hasMoreElements(); ) {
            entry = enumEntry.nextElement();
            if (entry.getName().startsWith(directoryName)) {
                Path pathToResourceInsideJar = fs.getPath("/"+entry.getName());

                File outputFile = new File(outputPath, entry.getName());
                if (!outputFile.exists()) {
                    outputFile.getParentFile().mkdirs();
                    outputFile = new File(outputPath, entry.getName());
                }

                if (entry.isDirectory()) {
                    continue;
                }

                Files.copy(pathToResourceInsideJar, outputFile.toPath());
            }
        }
    }

}
