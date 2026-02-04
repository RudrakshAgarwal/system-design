package com.designpatterns.builder;

import java.util.Map;

public class HttpAppTelescoping {
    public static void run() {
        HttpRequestTelescoping req1 = new HttpRequestTelescoping("https://api.example.com/data"); // GET, defaults
        HttpRequestTelescoping req2 = new HttpRequestTelescoping("https://api.example.com/submit", "POST", null, null, "{\"key\":\"value\"}");
        HttpRequestTelescoping req3 = new HttpRequestTelescoping("https://api.example.com/config", "PUT", Map.of("X-API-Key", "secret"), null, "config_data", 5000);
    }
}
