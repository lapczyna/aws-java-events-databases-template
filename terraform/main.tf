locals {
  name_prefix = "${var.project_name}-${var.environment}"
  common_tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

# ─── VPC ─────────────────────────────────────────────────────────────────────
module "vpc" {
  source = "./modules/vpc"

  name_prefix          = local.name_prefix
  vpc_cidr             = var.vpc_cidr
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  availability_zones   = var.availability_zones
}

# ─── EKS ─────────────────────────────────────────────────────────────────────
module "eks" {
  source = "./modules/eks"

  name_prefix         = local.name_prefix
  cluster_version     = var.eks_cluster_version
  vpc_id              = module.vpc.vpc_id
  private_subnet_ids  = module.vpc.private_subnet_ids
  node_desired_size   = var.eks_node_desired_size
  node_min_size       = var.eks_node_min_size
  node_max_size       = var.eks_node_max_size
  node_instance_types = var.eks_node_instance_types
}

# ─── RDS ─────────────────────────────────────────────────────────────────────
module "rds" {
  source = "./modules/rds"

  name_prefix        = local.name_prefix
  vpc_id             = module.vpc.vpc_id
  private_subnet_ids = module.vpc.private_subnet_ids
  instance_class     = var.rds_instance_class
  db_name            = var.rds_db_name
  username           = var.rds_username
  password           = var.rds_password
  eks_sg_id          = module.eks.node_security_group_id
}

# ─── DynamoDB ────────────────────────────────────────────────────────────────
module "dynamodb" {
  source = "./modules/dynamodb"

  table_name = var.dynamodb_table_name
}

# ─── SQS ─────────────────────────────────────────────────────────────────────
module "sqs" {
  source = "./modules/sqs"

  queue_name = var.sqs_queue_name
}

# ─── IAM / IRSA ──────────────────────────────────────────────────────────────
module "iam" {
  source = "./modules/iam"

  name_prefix        = local.name_prefix
  oidc_provider_arn  = module.eks.oidc_provider_arn
  oidc_provider_url  = module.eks.oidc_provider_url
  dynamodb_table_arn = module.dynamodb.table_arn
  sqs_queue_arn      = module.sqs.queue_arn
  github_org         = var.github_org
  github_repo        = var.github_repo
}

# ─── EKS Access Entry: GitHub Actions deployer ───────────────────────────────
# Grants the CD IAM role edit-scoped Kubernetes RBAC in the ad-platform
# namespace via the EKS Access Entries API (no aws-auth ConfigMap required).
resource "aws_eks_access_entry" "github_deployer" {
  cluster_name  = module.eks.cluster_name
  principal_arn = module.iam.github_actions_deploy_role_arn
  type          = "STANDARD"
}

resource "aws_eks_access_policy_association" "github_deployer" {
  cluster_name  = module.eks.cluster_name
  principal_arn = module.iam.github_actions_deploy_role_arn
  policy_arn    = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSEditPolicy"

  access_scope {
    type       = "namespace"
    namespaces = ["ad-platform"]
  }

  depends_on = [aws_eks_access_entry.github_deployer]
}
