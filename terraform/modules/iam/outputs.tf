output "irsa_role_arn" { value = aws_iam_role.irsa.arn }
output "irsa_role_name" { value = aws_iam_role.irsa.name }
output "github_actions_deploy_role_arn" { value = aws_iam_role.github_actions_deployer.arn }
