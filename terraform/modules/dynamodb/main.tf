resource "aws_dynamodb_table" "ad_impressions" {
  name         = var.table_name
  billing_mode = "PAY_PER_REQUEST"  # No upfront capacity planning; scales automatically
  hash_key     = "impressionId"

  attribute {
    name = "impressionId"
    type = "S"
  }

  # GSI: query by campaignId for reporting
  global_secondary_index {
    name            = "campaignId-index"
    hash_key        = "campaignId"
    projection_type = "ALL"
  }

  attribute {
    name = "campaignId"
    type = "S"
  }

  point_in_time_recovery {
    enabled = true
  }

  server_side_encryption {
    enabled = true
  }

  tags = { Name = var.table_name }
}
