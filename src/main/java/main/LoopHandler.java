package main;

import controller.LogHandler;

public class LoopHandler {
    private final int loopFreq; //hertz
    private final long loopPeriod; //nano seconds
    private Thread loopThread;
    private final Runnable updatable;
    private long timeOld=0;
    private boolean running, nonStopLoop;

    public LoopHandler(int loopFreq, Runnable updatable) {
        this.loopFreq = loopFreq;
        this.loopPeriod = (long)(1e9/loopFreq);
        this.updatable = updatable;
        running = true;
        nonStopLoop = false;
        loopThread = new Thread(this::run);
        timeOld = System.nanoTime();
        loopThread.start();
    }

    private void run() {
        while (running) {
            updatable.run();
            if(!nonStopLoop) synchronizeLoop();
        }
    }

    private void synchronizeLoop(){
        long timeNow = System.nanoTime();
        long timeDiff = timeNow-timeOld;
        //System.out.println(timeNow + "---" + timeOld);

        if(timeDiff<loopPeriod) {
            long timeRemain = loopPeriod-timeDiff;
            long nanos = (long)(timeRemain%1000000);
            long millis = (long)((timeRemain-nanos)/1000000);
            //System.out.println(millis + "...." + nanos);
            try {
                //System.out.println("thread sleep");
                Thread.sleep(millis, (int) nanos);
            } catch (InterruptedException e) {
                //e.printStackTrace();
                LogHandler.logger.error("Thread Interrupted: "+ e.getMessage());
            }
        }
        timeOld = System.nanoTime();
    }

    public void pause(){
        running = false;
    }

    public void resume(){
        running = true;
        loopThread = new Thread(this::run);
        loopThread.start();
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public boolean isNonStop(){
        return nonStopLoop;
    }

    public void setNonStop(boolean nonStop){
        nonStopLoop = nonStop;
    }
}