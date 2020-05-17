package br.com.brunoxkk0.injection;

import br.com.brunoxkk0.helper.downloader.IDownloadable;

import java.io.File;
import java.net.URL;

public class LibraryObj implements IDownloadable {

    private final File finalFolder;
    private final String fileName;
    private final String targetFileExtension;
    private final URL targetURL;

    public LibraryObj(File finalFolder, String fileName, String targetFileExtension, URL targetURL){

        this.finalFolder = finalFolder;
        this.fileName = fileName;
        this.targetFileExtension = targetFileExtension;
        this.targetURL = targetURL;

    }

    @Override
    public File getFinalFolder() {
        return finalFolder;
    }

    @Override
    public String getTargetFileName() {
        return fileName;
    }

    @Override
    public String getTargetFileExtension() {
        return targetFileExtension;
    }

    @Override
    public URL getTargetURL() {
        return targetURL;
    }

}
