package br.com.brunoxkk0.helper.downloader;


import br.com.brunoxkk0.helper.task.IProgress;
import br.com.brunoxkk0.helper.task.IRunnableTask;

import java.net.Proxy;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;



public class AsyncDownloadFactory implements IProgress, IRunnableTask {

    private Logger logger;
    private boolean isFinished = false;

    private String userAgent;
    private Object data;
    private LinkedList<DownloaderInstance<? extends IDownloadable>> downloaderInstance;
    private int simultaneousDownloads = 2;
    private ExecutorService executorService;

    public static AsyncDownloadFactory getInstance(){
        return new AsyncDownloadFactory("AsyncDownloadFactory");
    }

    public AsyncDownloadFactory(String userAgent){
        this.userAgent = userAgent;
        this.logger = Logger.getLogger(userAgent);

    }

    public AsyncDownloadFactory build(List<? extends IDownloadable> data, Proxy proxy){

        this.data = data;
        this.downloaderInstance = new LinkedList<>();

        for(IDownloadable downloadable : data){
            downloaderInstance.add(new DownloaderInstance<>(this,downloadable, proxy));
        }

        return this;
    }

    public AsyncDownloadFactory build(List<? extends IDownloadable> data){
        return build(data,null);
    }

    public AsyncDownloadFactory build(IDownloadable data, Proxy proxy){
        this.data = data;
        this.downloaderInstance = new LinkedList<>();
        this.downloaderInstance.add(new DownloaderInstance<>(this, data, proxy));

        return this;
    }

    public AsyncDownloadFactory build(IDownloadable data){
        return build(data,null);
    }

    public Logger getLogger() {
        return logger;
    }

    public void setSimultaneousDownloads(int simultaneousDownloads) {
        this.simultaneousDownloads = simultaneousDownloads;
    }

    @Override
    public double getCurrentProgress() {

        DoubleStream progress = downloaderInstance.stream().filter(downloaderInstance1 -> downloaderInstance1.getCurrentProgress() >= 0).mapToDouble(DownloaderInstance::getCurrentProgress);

        return (progress.sum() * 100) / (progress.count() * 100) / 10;
    }

    @Override
    public boolean isFinished() {
        return (executorService != null) && executorService.isTerminated();
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void download(){
        executorService = Executors.newFixedThreadPool(simultaneousDownloads);
        run();
    }

    @Override
    public void run() {

        logger.info("Putting downloads on queue.");

        for(DownloaderInstance<?> downloaderInstance : downloaderInstance){
            executorService.submit(new Thread(() -> {
                logger.info("Downloading " + downloaderInstance.getData().getTargetFileName());
                downloaderInstance.run();
            }));
        }


    }
}
