package pl.edu.pw.elka.phrasalwrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.*;
import java.security.Permission;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Utilities {

    public static void renameFile(String absoluteFilePath, String newName) throws IOException {
        Path source = Paths.get(absoluteFilePath);
        Files.move(source, source.resolveSibling(newName));
    }

    public static Path getResourcePath(String pathToResourceInsideJar) throws IOException {
        String pathToJar = Utilities.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPathToJar = URLDecoder.decode(pathToJar, "UTF-8");

        URI uri = URI.create("jar:file:"+decodedPathToJar);
        Map<String, String> env = new HashMap<>();
        FileSystem fs;
        try {
            fs = FileSystems.newFileSystem(uri, env);
        } catch (FileSystemAlreadyExistsException exp) {
            fs = FileSystems.getFileSystem(uri);
        }
        return fs.getPath(pathToResourceInsideJar);
    }

    public static void extractDirectory(String folderPath, String outputPath) throws IOException {
        String directoryName = folderPath.startsWith("/") ? folderPath.substring(1, folderPath.length()) : folderPath;

        String pathToJar = Utilities.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPathToJar = URLDecoder.decode(pathToJar, "UTF-8");
        JarFile jarFile = new JarFile(decodedPathToJar);

        URI uriToJar = URI.create("jar:file:"+decodedPathToJar);
        Map<String, String> env = new HashMap<>();
        FileSystem fs;
        try {
            fs = FileSystems.newFileSystem(uriToJar, env);
        } catch (FileSystemAlreadyExistsException exp) {
            fs = FileSystems.getFileSystem(uriToJar);
        }

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

    public static void printBashProcessOutput(Process process) throws IOException {
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        // read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        // read any errors from the attempted command
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }

        System.err.println("Exit status=" + process.exitValue());
    }

    public static class ExitTrappedException extends SecurityException { }

    public static void forbidSystemExitCall() {
        final SecurityManager securityManager = new SecurityManager() {
            public void checkPermission( Permission permission ) {
                if( permission.getName().startsWith("exitVM") ) {
                    throw new ExitTrappedException() ;
                }
            }
        } ;
        System.setSecurityManager( securityManager ) ;
    }

    public static void enableSystemExitCall() {
        System.setSecurityManager( null ) ;
    }

}
