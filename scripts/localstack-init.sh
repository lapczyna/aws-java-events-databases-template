#!/bin/bash
# LocalStack initialization: creates SQS queue and DynamoDB table
set -e

echo "==> Initializing LocalStack resources..."

AWS="aws --endpoint-url=http://localhost:4566 --region us-east-1"

# SQS FIFO Queue
$AWS sqs create-queue \
  --queue-name ad-events-queue.fifo \
  --attributes FifoQueue=true,ContentBasedDeduplication=false \
  || echo "Queue may already exist"

# Standard queue (non-FIFO fallback for development)
$AWS sqs create-queue \
  --queue-name ad-events-queue \
  || echo "Standard queue may already exist"

# DynamoDB table
$AWS dynamodb create-table \
  --table-name ad-impression-events \
  --attribute-definitions AttributeName=impressionId,AttributeType=S \
  --key-schema AttributeName=impressionId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  || echo "DynamoDB table may already exist"

echo "==> LocalStack initialization complete."
