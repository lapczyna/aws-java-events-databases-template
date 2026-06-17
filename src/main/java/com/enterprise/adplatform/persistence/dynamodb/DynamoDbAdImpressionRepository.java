package com.enterprise.adplatform.persistence.dynamodb;

import com.enterprise.adplatform.infrastructure.dynamodb.AdImpressionItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DynamoDbAdImpressionRepository {

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

    @Value("${aws.dynamodb.table-name:ad-impression-events}")
    private String tableName;

    private DynamoDbTable<AdImpressionItem> table() {
        return dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(AdImpressionItem.class));
    }

    public AdImpressionItem save(AdImpressionItem item) {
        log.debug("Saving AdImpressionItem with id={}", item.getImpressionId());
        table().putItem(item);
        return item;
    }

    public Optional<AdImpressionItem> findById(String impressionId) {
        log.debug("Looking up AdImpressionItem id={}", impressionId);
        Key key = Key.builder().partitionValue(impressionId).build();
        AdImpressionItem item = table().getItem(key);
        return Optional.ofNullable(item);
    }

    public List<AdImpressionItem> findAll() {
        log.debug("Scanning all AdImpressionItems");
        return table().scan(ScanEnhancedRequest.builder().build())
                .items()
                .stream()
                .collect(Collectors.toList());
    }

    public AdImpressionItem update(AdImpressionItem item) {
        log.debug("Updating AdImpressionItem id={}", item.getImpressionId());
        table().updateItem(item);
        return item;
    }

    public void deleteById(String impressionId) {
        log.debug("Deleting AdImpressionItem id={}", impressionId);
        Key key = Key.builder().partitionValue(impressionId).build();
        table().deleteItem(key);
    }

    public boolean existsById(String impressionId) {
        Key key = Key.builder().partitionValue(impressionId).build();
        return table().getItem(key) != null;
    }
}
