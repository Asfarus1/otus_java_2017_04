package ru.otus_matveev_anton.genaral;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClosingListener{
    private final List<Runnable> shutdownRegistrations = new CopyOnWriteArrayList<>();

    public void addShutdownRegistration(Runnable runnable) {
        this.shutdownRegistrations.add(runnable);
    }

    public void onClose() {
        for (Runnable runnable : shutdownRegistrations) {
            runnable.run();
        }
        shutdownRegistrations.clear();
    }
}
