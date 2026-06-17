package com.example.api_gateway.component;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RouteDebugRunner implements CommandLineRunner {

    private final RouteDefinitionLocator locator;

    @Override
    public void run(String... args) {

        locator.getRouteDefinitions()
                .doOnNext(route ->
                        System.out.println("ROUTE FOUND -> " + route))
                .subscribe();
    }
}