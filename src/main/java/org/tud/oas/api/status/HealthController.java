package org.tud.oas.api.status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.tud.oas.util.context.RequestContext;

@RestController
@RequestMapping("/v1/health")
public class HealthController {
    private final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    public HealthController() {
    }

    @GetMapping
    public HealthResponse health() {
        String tenancy = RequestContext.getTenancy();
        logger.info("Health check called with tenancy {}", tenancy);

        return new HealthResponse(true);
    }
}
