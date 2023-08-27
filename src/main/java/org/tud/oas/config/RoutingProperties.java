package org.tud.oas.config;

import java.util.List;
import java.util.Map;

public class RoutingProperties {
    private List<String> providers;
    private String defaultProvider;
    private Map<String, Map<String, String>> providerOptions;

    public List<String> getProviders() {
        return providers;
    }

    public void setProviders(List<String> providers) {
        this.providers = providers;
    }

    public String getDefaultProvider() {
        return defaultProvider;
    }

    public void setDefaultProvider(String defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public Map<String, Map<String, String>> getProviderOptions() {
        return providerOptions;
    }

    public void setProviderOptions(Map<String, Map<String, String>> providerOptions) {
        this.providerOptions = providerOptions;
    }
}
