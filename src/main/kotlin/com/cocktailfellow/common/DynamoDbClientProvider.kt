package com.cocktailfellow.common

import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.regions.Region
import java.net.URI

object DynamoDbClientProvider {
  private val dynamoDbEndpoint = System.getenv("DYNAMODB_ENDPOINT")
  private val region = System.getenv("REGION") ?: "eu-central-1"

  fun get(): DynamoDbClient {
    return if (dynamoDbEndpoint.isNullOrEmpty()) {
      DynamoDbClient.builder()
        .region(Region.of(region))
        .build()
    } else {
      DynamoDbClient.builder()
        .endpointOverride(URI.create(dynamoDbEndpoint))
        .region(Region.of(region))
        .build()
    }
  }
}
