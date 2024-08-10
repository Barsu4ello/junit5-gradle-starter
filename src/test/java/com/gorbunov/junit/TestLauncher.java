package com.gorbunov.junit;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.io.PrintWriter;

public class TestLauncher {

    public static void main(String[] args) {
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener summaryGeneratingListener = new SummaryGeneratingListener();
//        launcher.registerLauncherDiscoveryListeners(); // Настройка лисенеров
//        launcher.registerTestExecutionListeners(summaryGeneratingListener); // Настройка лисенеров

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
                .request()
//                .selectors(DiscoverySelectors.selectClass(UserServiceTest.class)) // надо делать тогда класс публичным
                .selectors(DiscoverySelectors.selectPackage("com.gorbunov.junit.service"))
//                .listeners() // так же можно настроить лиссенеры
                .build();
        launcher.execute(request, summaryGeneratingListener);

        try (PrintWriter printWriter = new PrintWriter(System.out)) {
            summaryGeneratingListener.getSummary().printTo(printWriter);
        }

    }
}
