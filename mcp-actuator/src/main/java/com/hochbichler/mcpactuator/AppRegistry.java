package com.hochbichler.mcpactuator;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppRegistry {

    private final Map<String, String> apps = new LinkedHashMap<>();

    public AppRegistry(@Value("${apps:}") String appsArg) {
        if (!appsArg.isBlank()) {
            for (String url : appsArg.split(",")) {
                url = url.trim();
                String name = extractAppName(url);
                apps.put(name, url);
            }
        }
    }

    private String extractAppName(String url) {
        URI uri = URI.create(url);
        String host = uri.getHost();
        int port = uri.getPort();
        return host + (port > 0 ? ":" + port : "");
    }

    public Map<String, String> getApps() {
        return Collections.unmodifiableMap(apps);
    }

    public String getUrl(String appName) {
        return apps.get(appName);
    }
}
