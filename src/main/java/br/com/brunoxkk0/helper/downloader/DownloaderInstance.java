package br.com.brunoxkk0.helper.downloader;


import br.com.brunoxkk0.helper.task.IProgress;
import br.com.brunoxkk0.helper.task.IRunnableTask;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URLConnection;
import java.util.Optional;

public class DownloaderInstance< T extends IDownloadable> implements IProgress, IRunnableTask {

    private double progress = 0;
    private boolean isFinished = false;
    private Exception exception;
    private T data;
    private Proxy proxy;

    private Long contentLength = 0L;
    private Optional<String> finalMD5Hash;

    private AsyncDownloadFactory factory;

    public DownloaderInstance(AsyncDownloadFactory factory, T data){
        this.factory = factory;
        this.data = data;
    }

    public DownloaderInstance(AsyncDownloadFactory factory, T data, Proxy proxy){
        this.factory = factory;
        this.data = data;
        this.proxy = proxy;
    }

    @Override
    public double getCurrentProgress() {
        return progress;
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public Optional<String> getFinalMD5Hash() {
        return finalMD5Hash;
    }

    public Exception getException() {
        return exception;
    }

    public T getData() {
        return data;
    }

    @Override
    public void run() {
        try{
            URLConnection connection;

            if(proxy != null){
                connection = data.getTargetURL().openConnection(proxy);
            }else {
                connection = data.getTargetURL().openConnection();
            }

            File folder;
            if((folder = data.getFinalFolder()) != null){

                if(!folder.exists()){

                    if(!folder.exists()){
                        factory.getLogger().info("[" + folder.getName()+ "] Not exists, making directory...");
                        if(folder.mkdirs()) factory.getLogger().info("Directory created.");
                    }
                }
            }

            if(connection != null){

                connection.setRequestProperty("User-Agent", factory.getUserAgent());

                InputStream is = connection.getInputStream();
                contentLength = connection.getContentLengthLong();

                String fileName = (data.getTargetFileName().endsWith(data.getTargetFileExtension())) ? data.getTargetFileName() : (data.getTargetFileName() + data.getTargetFileExtension());

                File file = new File(folder, fileName);
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                int bytes;

                while ((bytes = is.read()) != -1) {
                    fileOutputStream.write(bytes);
                    progress = ((fileOutputStream.getChannel().size() * 100D) / contentLength);
                }

                finalMD5Hash = Optional.of(Files.hash(file, Hashing.md5()).toString());

                exception = null;
                progress = 1;
                isFinished = true;
            }

        } catch (IOException e) {
            exception = e;
            progress = -1;
            isFinished = true;
        }
    }
}
