package com.hochbichler.mcpactuator;

import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ActuatorMcpResources {

    private final AppRegistry appRegistry;
    private final RestClient restClient;

    public ActuatorMcpResources(AppRegistry appRegistry) {
        this.appRegistry = appRegistry;
        this.restClient = RestClient.create();
    }

    @McpResource(
        uri = "apps://info",
        name = "App Registry",
        description = "Registered apps and their static build info from /actuator/info")
    public String getAppInfo() {
        var sb = new StringBuilder();
        for (var entry : appRegistry.getApps().entrySet()) {
            String name = entry.getKey();
            String url = entry.getValue();
            sb.append("=== ").append(name).append(" ===\n");
            sb.append("URL: ").append(url).append("\n");
            try {
                String info = restClient.get()
                    .uri(url + "/actuator/info")
                    .retrieve()
                    .body(String.class);
                sb.append(info).append("\n");
            } catch (RestClientException e) {
                sb.append("info: not available\n");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }
}
