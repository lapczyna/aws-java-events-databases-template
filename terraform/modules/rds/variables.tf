variable "name_prefix" { type = string }
variable "vpc_id" { type = string }
variable "private_subnet_ids" { type = list(string) }
variable "instance_class" { type = string }
variable "db_name" { type = string }
variable "username" {
  type      = string
  sensitive = true
}
variable "password" {
  type      = string
  sensitive = true
}
variable "eks_sg_id" { type = string }
