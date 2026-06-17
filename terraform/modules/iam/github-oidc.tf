# ─── GitHub Actions OIDC ──────────────────────────────────────────────────────
# Allows GitHub Actions jobs to exchange their OIDC token for short-lived AWS
# credentials — no long-lived AWS secrets stored in GitHub.

resource "aws_iam_openid_connect_provider" "github_actions" {
  url             = "https://token.actions.githubusercontent.com"
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = ["6938fd4d98bab03faadb97b34396831e3780aea1"]
}

data "aws_iam_policy_document" "github_actions_assume" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]
    effect  = "Allow"

    principals {
      type        = "Federated"
      identifiers = [aws_iam_openid_connect_provider.github_actions.arn]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }

    # Restrict to semver tag pushes on this repository only.
    condition {
      test     = "StringLike"
      variable = "token.actions.githubusercontent.com:sub"
      values   = ["repo:${var.github_org}/${var.github_repo}:ref:refs/tags/v*"]
    }
  }
}

resource "aws_iam_role" "github_actions_deployer" {
  name               = "${var.name_prefix}-github-deployer"
  assume_role_policy = data.aws_iam_policy_document.github_actions_assume.json
}

# Least-privilege: only the permission needed to fetch kubeconfig.
# Kubernetes RBAC (EKS Access Entry below) controls what kubectl can do.
resource "aws_iam_policy" "eks_describe" {
  name = "${var.name_prefix}-eks-describe-policy"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Sid      = "EksDescribeCluster"
      Effect   = "Allow"
      Action   = ["eks:DescribeCluster"]
      Resource = "*"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "eks_describe" {
  role       = aws_iam_role.github_actions_deployer.name
  policy_arn = aws_iam_policy.eks_describe.arn
}
