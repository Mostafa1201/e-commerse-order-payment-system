package com.exequt.checkout.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MockProviderService {

    private static final Logger log = LoggerFactory.getLogger(MockProviderService.class);

    private final RestTemplate restTemplate;

    @Value("${app.base-url}")
    private String baseUrl;

    public MockProviderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    

}
