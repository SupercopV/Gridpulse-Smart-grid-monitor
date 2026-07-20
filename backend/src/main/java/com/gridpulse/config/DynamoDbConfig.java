package com.gridpulse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDbConfig {

    @Value("${gridpulse.dynamodb.mock:true}")
    private boolean isMock;

    @Value("${gridpulse.dynamodb.aws.region:us-east-1}")
    private String region;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        if (isMock) {
            
            return null;
        }
        try {
            return DynamoDbClient.builder()
                    .region(Region.of(region))
                    .build();
        } catch (Exception e) {
            
            System.err.println("Failed to initialize AWS DynamoDbClient: " + e.getMessage() + ". Defaulting to local repository mode.");
            return null;
        }
    }
}
