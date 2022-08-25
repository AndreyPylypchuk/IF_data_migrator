package com.tht.ifdatamigrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

@Configuration
public class Config {

    @Bean
    public RestTemplate template() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper mapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
