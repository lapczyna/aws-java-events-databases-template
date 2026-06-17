output "endpoint" {
  value     = aws_db_instance.this.endpoint
  sensitive = true
}
output "db_name"        { value = aws_db_instance.this.db_name }
output "instance_id"    { value = aws_db_instance.this.id }
