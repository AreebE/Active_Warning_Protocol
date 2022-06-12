package com.example.recievingeventapp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
public class RequestHandler {

    private class Request implements Runnable
    {
        private Runnable r;
        private long delay;
        private boolean continueUntilDeath;

        public Request(Runnable r, long delay, boolean continueUntilDeath)
        {
            this.r = r;
            this.continueUntilDeath = continueUntilDeath;
            this.delay = delay;
        }

        @Override
        public void run() {
            r.run();
            if (continueUntilDeath)
            {
                runRequest(this, delay);
            }
        }
    }
    private ExecutorService executorService;
    private Handler handler;

    public RequestHandler()
    {
        executorService = Executors.newSingleThreadExecutor();
        handler = new Handler();
    }

    public void runRequest(Runnable r, long delay, boolean repeatUntilDeath)
    {
        runRequest(new Request(r, delay, repeatUntilDeath), 0);
    }

    private void runRequest(Request r, long delay)
    {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                executorService.execute(r);
            }
        }, delay);
    }

    public void stop()
    {
        handler.removeCallbacks(null);
    }

    public void endPermanently()
    {
        handler.removeCallbacks(null);
        executorService.shutdown();
    }


}
