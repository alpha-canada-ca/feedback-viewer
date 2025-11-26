output "docdb_username_arn" {
  description = "The SSM parameter ARN for the DocumentDB username"
  value       = aws_ssm_parameter.docdb_username.arn
}

output "docdb_password_arn" {
  description = "The SSM parameter ARN for the DocumentDB password"
  value       = aws_ssm_parameter.docdb_password.arn
}

output "jwt_secret_key_arn" {
  description = "The SSM parameter ARN for the JWT secret key"
  value       = aws_ssm_parameter.jwt_secret_key.arn
}