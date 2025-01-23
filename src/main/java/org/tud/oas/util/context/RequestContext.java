package org.tud.oas.util.context;

public class RequestContext {
    private static final ThreadLocal<String> TENANCY_CONTEXT = new ThreadLocal<>();

    public static void setTenancy(String tenancy) {
        TENANCY_CONTEXT.set(tenancy);
    }

    public static String getTenancy() {
        return TENANCY_CONTEXT.get();
    }

    public static void clear() {
        TENANCY_CONTEXT.remove();
    }
}
