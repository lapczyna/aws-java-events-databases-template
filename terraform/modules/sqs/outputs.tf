output "queue_url"  { value = aws_sqs_queue.ad_events.url }
output "queue_arn"  { value = aws_sqs_queue.ad_events.arn }
output "dlq_url"    { value = aws_sqs_queue.ad_events_dlq.url }
output "dlq_arn"    { value = aws_sqs_queue.ad_events_dlq.arn }
