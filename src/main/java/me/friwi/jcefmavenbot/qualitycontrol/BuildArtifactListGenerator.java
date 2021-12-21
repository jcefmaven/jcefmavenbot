package me.friwi.jcefmavenbot.qualitycontrol;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class BuildArtifactListGenerator {
    private static final File tempDir = new File("temp");
    private static final File artifactFile = new File(tempDir, "artifact.tar.gz");
    private static final File artifactDir = new File(tempDir, "ext");
    private static final int BUFFER_SIZE = 4096;

    public static BuildArtifactList generate(String url) throws IOException {
        //Clean tempDir
        deleteDir(tempDir);
        tempDir.mkdirs();
        //Download file
        downloadFile(url, artifactFile);
        //Extract file
        artifactDir.mkdirs();
        extractTarGZ(artifactDir, new FileInputStream(artifactFile));
        //Generate build artifact list
        BuildArtifactList list = generate(artifactDir);
        //Clear tempDir
        deleteDir(tempDir);
        //Return
        return list;
    }

    private static BuildArtifactList generate(File artifactDir) {
        BuildArtifactList list = new BuildArtifactList();
        String path = "/";
        recursiveGenerate(artifactDir, artifactDir, path, list);
        return list;
    }

    private static void recursiveGenerate(File mainDir, File dir, String path, BuildArtifactList list) {
        if(dir.isFile()){
            list.addElement(new BuildArtifactElement(path+dir.getName(), dir.length()));
        }else{
            Arrays.stream(dir.listFiles()).sorted((f1, f2)->{
                if((f1.isDirectory() && f2.isDirectory()) || (f1.isFile() && f2.isFile())){
                    return f1.getName().compareTo(f2.getName());
                }else{
                    if(f1.isDirectory())return 1;
                    else return -1;
                }
            }).forEach(x->recursiveGenerate(mainDir, x, path+(dir.equals(mainDir)?"":(dir.getName()+"/")), list));
        }
    }

    private static void downloadFile(String url, File file) throws IOException {
        URLConnection conn = new URL(url).openConnection();
        InputStream in = conn.getInputStream();
        if(!file.exists()){
            if(!file.createNewFile()){
                throw new IOException("Failed to create file "+file);
            }
        }
        FileOutputStream fos = new FileOutputStream(file);
        int r;
        byte[] buff = new byte[BUFFER_SIZE];
        while((r=in.read(buff))!=-1){
            fos.write(buff, 0, r);
        }
        fos.flush();
        in.close();
        fos.close();
    }

    private static void deleteDir(File dir) throws IOException {
        if(!dir.exists())return;
        if(dir.isDirectory()) {
            File[] cont = dir.listFiles();
            if (cont == null) {
                if (!dir.delete()){
                    throw new IOException("Could not delete dir " + dir);
                }
                throw new IOException("Could not list contents of dir " + dir);
            }
            for (File x : cont) {
                deleteDir(x);
            }
        }
        if (!dir.delete()){
            throw new IOException("Could not delete file " + dir);
        }
    }

    public static void extractTarGZ(File installDir, InputStream in) throws IOException {
        Objects.requireNonNull(installDir, "installDir cannot be null");
        Objects.requireNonNull(in, "in cannot be null");
        GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(in);
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
            TarArchiveEntry entry;

            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                File f = new File(installDir, entry.getName());
                if (entry.isDirectory()) {
                    boolean created = f.mkdir();
                    if (!created) {
                        System.out.println(String.format("Unable to create directory '%s', during extraction of archive contents.\n",
                                f.getAbsolutePath()));
                    } else {
                        if ((entry.getMode() & 0111) != 0 && !f.setExecutable(true, false)) {
                            System.out.println(String.format("Unable to mark directory '%s' executable, during extraction of archive contents.\n",
                                    f.getAbsolutePath()));
                        }
                    }
                } else {
                    int count;
                    byte[] data = new byte[BUFFER_SIZE];
                    FileOutputStream fos = new FileOutputStream(f, false);
                    try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {
                        while ((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
                            dest.write(data, 0, count);
                        }
                    }
                    if ((entry.getMode() & 0111) != 0 && !f.setExecutable(true, false)) {
                        System.out.println(String.format("Unable to mark file '%s' executable, during extraction of archive contents.\n",
                                f.getAbsolutePath()));
                    }
                }
            }
        }
    }
}
