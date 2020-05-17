package br.com.brunoxkk0.helper.task;

public interface ReturnableTask <T> extends IRunnableTask, IProgress{

    T getResult();

    boolean isResultReady();

}
