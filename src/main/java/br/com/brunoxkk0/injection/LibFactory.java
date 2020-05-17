package br.com.brunoxkk0.injection;

import br.com.brunoxkk0.helper.downloader.AsyncDownloadFactory;
import br.com.brunoxkk0.helper.downloader.IDownloadable;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

public class LibFactory {

    private static final File targetFolder = new File("SyrxCore/Libraries/");

    public static LibFactory instance;

    public static LibFactory getInstance(){
        return (instance != null) ? instance : new LibFactory();
    }

    private LinkedList<LibraryObj> libs;

    private LibFactory(){
        instance = this;
        libs = new LinkedList<>();
    }

    public void load(){

        AsyncDownloadFactory asyncDownloadFactory = AsyncDownloadFactory.getInstance();
        ArrayList<IDownloadable> iDownloadables = new ArrayList<>();

        if(targetFolder.exists() && targetFolder.canRead()){
            for(LibraryObj libraryObj : libs){
                if(!isAvailable(libraryObj, targetFolder)){
                    iDownloadables.add(libraryObj);
                }
            }

            asyncDownloadFactory.build(iDownloadables);

            asyncDownloadFactory.download();

            asyncDownloadFactory.getLogger().info("[LibFactory] - Starting Download");

            while (asyncDownloadFactory.isFinished()){

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                asyncDownloadFactory.getLogger().info("[LibFactory] - Downloading >> " + ((int)asyncDownloadFactory.getCurrentProgress()) + "%");

            }

            asyncDownloadFactory.getLogger().info("[LibFactory] - Download finished");

            return;

        }

        throw new RuntimeException("Fail to load libraries factory.");
    }

    public void inject(ClassLoader target){
        new ClassInjector(target, targetFolder.getName());
    }

    public boolean isAvailable(LibraryObj library, File targetFolder) {
        File file = new File(targetFolder, (library.getTargetFileName().endsWith(library.getTargetFileExtension())) ? library.getTargetFileName() : library.getTargetFileName() + library.getTargetFileExtension());
        return file.exists() && file.canRead();
    }



}
