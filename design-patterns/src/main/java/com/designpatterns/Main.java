package com.designpatterns;

import com.designpatterns.builder.HttpAppBuilder;
import com.designpatterns.builder.HttpAppTelescoping;

public class Main {
    public static void main(String[] args) {
        /* Design Patterns */
        HttpAppBuilder.run();
        HttpAppTelescoping.run();
    }
}
