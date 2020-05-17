package br.com.brunoxkk0.injection;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class FileLoader {

    private ArrayList<File> files;

    public FileLoader(String path){

        files = new ArrayList<>();

        File folder = new File(path);

        if(folder.isDirectory() & (folder.exists() || folder.mkdirs())) {
            searchJars(folder);
        }

    }

    public void searchJars(File file){

        if(file.exists() && file.canRead() && file.isDirectory()){
            for(File aq : file.listFiles()){
                searchJars(aq);
            }
        }else {
            if(file.canRead() && file.getName().endsWith(".jar")){
                files.add(file);
                System.out.println("[FileLoader] - Carregado: " + file.getName());
            }
        }
    }

    public URL[] getFilesUrl() throws MalformedURLException {
        ArrayList<URL> urls = new ArrayList<>();

        for(File file : getFiles()){
            urls.add(file.toURI().toURL());
        }

        return urls.toArray(new URL[0]);
    }

    public ArrayList<File> getFiles() {
        return files;
    }

    public int getSize(){
        return files.size();
    }
}
