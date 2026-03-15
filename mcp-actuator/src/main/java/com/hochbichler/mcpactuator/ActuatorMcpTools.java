package com.hochbichler.mcpactuator;

import java.util.Map;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ActuatorMcpTools {

    private final AppRegistry appRegistry;
    private final RestClient restClient;

    public ActuatorMcpTools(AppRegistry appRegistry) {
        this.appRegistry = appRegistry;
        this.restClient = RestClient.create();
    }

    @McpTool(
        name = "check-health",
        description = "Check the health of a monitored Spring Boot application. "
            + "Leave appName empty to check all apps.")
    public String checkHealth(
            @McpToolParam(description = "App name (e.g. localhost:8080) or leave empty to check all",
                          required = false)
            String appName) {

        if (appName == null || appName.isBlank()) {
            return checkAllApps();
        }

        String url = appRegistry.getUrl(appName);
        if (url == null) {
            return "Unknown app: " + appName
                + ". Registered apps: " + appRegistry.getApps().keySet();
        }

        return fetchHealth(appName, url);
    }

    private String checkAllApps() {
        var sb = new StringBuilder();
        for (var entry : appRegistry.getApps().entrySet()) {
            sb.append(fetchHealth(entry.getKey(), entry.getValue())).append("\n");
        }
        return sb.toString().trim();
    }

    private String fetchHealth(String name, String url) {
        try {
            String response = restClient.get()
                .uri(url + "/actuator/health")
                .retrieve()
                .body(String.class);
            return name + " (" + url + "): " + response;
        } catch (RestClientException e) {
            return name + " (" + url + "): DOWN — " + e.getMessage();
        }
    }

    @McpTool(
        name = "get-metric",
        description = "Get a specific metric from a monitored app. "
            + "Common metrics: jvm.memory.used, http.server.requests, "
            + "system.cpu.usage, process.uptime",
        annotations = @McpTool.McpAnnotations(
            readOnlyHint = true,
            destructiveHint = false
        ))
    public String getMetric(
            @McpToolParam(description = "App name (e.g. localhost:8080)",
                          required = true)
            String appName,
            @McpToolParam(description = "Metric name, e.g. jvm.memory.used",
                          required = true)
            String metricName) {

        String url = appRegistry.getUrl(appName);
        if (url == null) {
            return "Unknown app: " + appName
                + ". Registered apps: " + appRegistry.getApps().keySet();
        }

        try {
            String response = restClient.get()
                .uri(url + "/actuator/metrics/" + metricName)
                .retrieve()
                .body(String.class);
            return appName + " — " + metricName + ": " + response;
        } catch (RestClientException e) {
            return "Failed to fetch " + metricName + " from " + appName
                + ": " + e.getMessage();
        }
    }

    @McpTool(
        name = "list-metrics",
        description = "List all available metric names for a monitored app",
        annotations = @McpTool.McpAnnotations(readOnlyHint = true))
    public String listMetrics(
            @McpToolParam(description = "App name (e.g. localhost:8080)",
                          required = true)
            String appName) {

        String url = appRegistry.getUrl(appName);
        if (url == null) {
            return "Unknown app: " + appName
                + ". Registered apps: " + appRegistry.getApps().keySet();
        }

        try {
            return restClient.get()
                .uri(url + "/actuator/metrics")
                .retrieve()
                .body(String.class);
        } catch (RestClientException e) {
            return "Failed to fetch metrics from " + appName + ": " + e.getMessage();
        }
    }
}
