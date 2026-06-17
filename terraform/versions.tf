terraform {
  required_version = ">= 1.9"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.70"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.33"
    }
  }

  # Remote state — uncomment and configure for team use
  # backend "s3" {
  #   bucket         = "your-terraform-state-bucket"
  #   key            = "ad-platform/terraform.tfstate"
  #   region         = "us-east-1"
  #   dynamodb_table = "terraform-locks"
  #   encrypt        = true
  # }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "ad-platform"
      ManagedBy   = "terraform"
      Environment = var.environment
    }
  }
}
