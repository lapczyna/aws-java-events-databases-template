locals {
  oidc_sub = replace(var.oidc_provider_url, "https://", "")
}

data "aws_iam_policy_document" "irsa_assume_role" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]
    effect  = "Allow"

    principals {
      type        = "Federated"
      identifiers = [var.oidc_provider_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "${local.oidc_sub}:sub"
      values   = ["system:serviceaccount:ad-platform:ad-platform-sa"]
    }

    condition {
      test     = "StringEquals"
      variable = "${local.oidc_sub}:aud"
      values   = ["sts.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "irsa" {
  name               = "${var.name_prefix}-irsa-role"
  assume_role_policy = data.aws_iam_policy_document.irsa_assume_role.json
}

# Least-privilege: DynamoDB access to the specific table only
resource "aws_iam_policy" "dynamodb" {
  name = "${var.name_prefix}-dynamodb-policy"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Sid    = "DynamoDBTableAccess"
      Effect = "Allow"
      Action = [
        "dynamodb:GetItem",
        "dynamodb:PutItem",
        "dynamodb:UpdateItem",
        "dynamodb:DeleteItem",
        "dynamodb:Query",
        "dynamodb:Scan",
        "dynamodb:BatchGetItem",
        "dynamodb:BatchWriteItem",
        "dynamodb:DescribeTable"
      ]
      Resource = [
        var.dynamodb_table_arn,
        "${var.dynamodb_table_arn}/index/*"
      ]
    }]
  })
}

# Least-privilege: SQS access to the specific queue only
resource "aws_iam_policy" "sqs" {
  name = "${var.name_prefix}-sqs-policy"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Sid    = "SqsQueueAccess"
      Effect = "Allow"
      Action = [
        "sqs:SendMessage",
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:GetQueueAttributes",
        "sqs:GetQueueUrl"
      ]
      Resource = [var.sqs_queue_arn]
    }]
  })
}

resource "aws_iam_role_policy_attachment" "dynamodb" {
  role       = aws_iam_role.irsa.name
  policy_arn = aws_iam_policy.dynamodb.arn
}

resource "aws_iam_role_policy_attachment" "sqs" {
  role       = aws_iam_role.irsa.name
  policy_arn = aws_iam_policy.sqs.arn
}
