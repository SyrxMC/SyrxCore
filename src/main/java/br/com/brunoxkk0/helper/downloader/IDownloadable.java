package br.com.brunoxkk0.helper.downloader;

import java.io.File;
import java.net.URL;

public interface IDownloadable {

    File getFinalFolder();

    String getTargetFileName();

    String getTargetFileExtension();

    URL getTargetURL();

}
