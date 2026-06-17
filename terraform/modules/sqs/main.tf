resource "aws_sqs_queue" "ad_events_dlq" {
  name                      = "${var.queue_name}-dlq"
  message_retention_seconds = 1209600 # 14 days

  tags = { Name = "${var.queue_name}-dlq" }
}

resource "aws_sqs_queue" "ad_events" {
  name                       = var.queue_name
  visibility_timeout_seconds = 60
  message_retention_seconds  = 86400 # 1 day
  receive_wait_time_seconds  = 20    # Long polling

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.ad_events_dlq.arn
    maxReceiveCount     = 5
  })

  tags = { Name = var.queue_name }
}

resource "aws_sqs_queue_policy" "ad_events" {
  queue_url = aws_sqs_queue.ad_events.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "AllowSendFromSameAccount"
        Effect    = "Allow"
        Principal = { AWS = "*" }
        Action    = ["sqs:SendMessage", "sqs:ReceiveMessage", "sqs:DeleteMessage", "sqs:GetQueueAttributes"]
        Resource  = aws_sqs_queue.ad_events.arn
        Condition = {
          ArnLike = { "aws:SourceAccount" = data.aws_caller_identity.current.account_id }
        }
      }
    ]
  })
}

data "aws_caller_identity" "current" {}
