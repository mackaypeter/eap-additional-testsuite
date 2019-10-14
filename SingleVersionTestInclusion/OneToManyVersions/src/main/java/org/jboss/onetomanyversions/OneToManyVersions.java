/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.onetomanyversions;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author panos
 * @author pmackay@redhat.com
 */
public class OneToManyVersions {

    private static List<JavaFile> javaFilesList = new ArrayList<>();
    private static List<String> otherResourcesList = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        String fileName = System.getProperty("MultipleVersionsFilePath", "/home/pmackay/praca/git/eap-additional-testsuite/SingleVersionTestInclusion/eap/src/test/java/org/jboss/additional/testsuite/jdkall/present/concurrency/");
        String destination = System.getProperty("MultipleVersionsDestination", "/home/pmackay/praca/git/eap-additional-testsuite/modules/src/main/java");

        getJavaFiles(fileName);
        for (JavaFile javaFile : javaFilesList) {
            processJavaFile(javaFile, destination);
        }

        for (String resourceFile : otherResourcesList) {
            System.out.println("Not yet implemented type of file: " + resourceFile);
        }

    }

    // TODO: rename and handle other types in a separate method?
    private static List<String> getJavaFiles(String file) throws IOException {
        List<String> javaFiles = new ArrayList<String>();

        Path rootPath = Paths.get(file);
        // lets preserve the existing single file copy functionality
        // TODO: single file copy of other resource types?
        if (Files.isRegularFile(rootPath) && rootPath.toString().endsWith("java")) {
            javaFiles.add(file);
        } else if (Files.isDirectory(rootPath)) {
            Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().endsWith("java")) {
                        javaFilesList.add(checkFileForVersions(file.toString()));
                    } else {
                        otherResourcesList.add(file.toString());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            throw new IllegalArgumentException("Wrong filePath was specified: " + rootPath.toString());
        }

        return javaFiles;
    }

    // TODO: move this to JavaFile, so we can refactor reasonable
    private static JavaFile checkFileForVersions(String file) {
        String[] destinations = null;
        ArrayList<FileData> result = new ArrayList<FileData>();
        File f = new File(file);
        ArrayList<String> servers = new ArrayList<>();
        ArrayList<String> modules = new ArrayList<>();
        ArrayList<String> lowerlimit = new ArrayList<>();
        ArrayList<String> upperlimit = new ArrayList<>();
        String packageName = null;

        int linenum = 0;
        int packagenum = 0;

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {

                if (line.matches("^(public |private )?class .*")) {
                    // nothing important past the class name definition
                    break;
                }

                if (line.contains("EATSERVERS") || line.contains("EATSERVERMODULE") || line.contains("EATLOWERLIMITVERSIONS") || line.contains("EATUPPERLIMITVERSIONS")) {
                    destinations = line.split(":")[1].split(",");
                    for (String elem : destinations) {
                        if (line.contains("EATSERVERS")) {
                            servers.add(elem.trim());
                        } else if (line.contains("EATSERVERMODULE")) {
                            modules.add(elem.trim());
                        } else if (line.contains("EATLOWERLIMITVERSIONS")) {
                            lowerlimit.add(elem.trim());
                        } else if (line.contains("EATUPPERLIMITVERSIONS")) {
                            upperlimit.add(elem.trim());
                        }
                    }
                }

                linenum++;
                if (line.startsWith("package")) {
                    packageName = line.replaceAll("package ", "").replaceAll(";", "").trim();
                    packagenum = linenum;
                }
            }

            for (int i = 0; i < servers.size(); i++) {
            //for (String server : servers) {
                String server = servers.get(i);
                String module = null;
                if (modules.size() == 1) {
                    module = modules.get(0);
                } else {
                    // TODO: can I have per server module definition? does it make sense?
                    module = modules.get(i);
                }
                String lower = null;
                // TODO: else what??
                if (lowerlimit.size() == servers.size()) {
                    lower = lowerlimit.get(i);
                }
                String upper = null;
                // TODO: same
                if (upperlimit.size() == servers.size()) {
                    upper = upperlimit.get(i);
                }

                FileData fd = new FileData(file, lower, upper, server, module, packageName);
                result.add(fd);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return new JavaFile(file, result, linenum, packagenum);
    }

    private static void processJavaFile(JavaFile file, String destination) throws IOException {
        String annotationparam = "";
        String packageName = null;
        List<FileData> fd = file.getFileData();

        for (FileData data : fd) {
            if (annotationparam.compareTo("") != 0) {
                annotationparam = annotationparam + ",";
            }
            packageName = data.packageName.replaceAll("\\.", "\\/");
            System.out.println(packageName);
            annotationparam = annotationparam + "\"modules/testcases/jdkAll/" + data.server + "/" + data.module + "/src/main/java";
            if (data.minVersion != null) {
                annotationparam = annotationparam + "#" + data.minVersion;
            } else if(!annotationparam.endsWith("\"")){
                annotationparam = annotationparam + "\"";
            }

            if (data.maxVersion != null) {
                annotationparam = annotationparam + "*" + data.maxVersion + "\"";
            } else if(!annotationparam.endsWith("\"")){
                annotationparam = annotationparam + "\"";
            }

            System.out.println(data.fileName + " " + data.server + " " + data.module + " " + data.minVersion + " " + data.maxVersion + " " + data.packageName);
        }

        String fileName = file.getFileName();

        String annotation = "@EapAdditionalTestsuite({" + annotationparam + "})";
        List<String> list = Files.readAllLines(new File(fileName).toPath(), Charset.defaultCharset());
        list.add(file.getLinenum(), annotation);
        list.add(file.getPackagenum(), "import org.jboss.eap.additional.testsuite.annotations.EapAdditionalTestsuite;");

        Path dir = Paths.get(destination+"/"+packageName);
        Path out = Paths.get(destination+"/"+packageName+"/"+fileName.substring(fileName.lastIndexOf("/")));
        System.out.println(out.toString());
        if(Files.notExists(out)) {
            Files.createDirectories(dir);
            Files.createFile(out);
        }
        Files.write(out, list, Charset.defaultCharset());
    }

}
