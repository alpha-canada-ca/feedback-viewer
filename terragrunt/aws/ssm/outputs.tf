output "docdb_username" {
  description = "The SSM parameter ARN for the DocumentDB username"
  value       = aws_ssm_parameter.docdb_username.arn
}

output "docdb_password" {
  description = "The SSM parameter ARN for the DocumentDB password"
  value       = aws_ssm_parameter.docdb_password.arn
}

output "jwt_secret_key" {
  description = "The SSM parameter ARN for the JWT secret key"
  value       = aws_ssm_parameter.jwt_secret_key.arn
}