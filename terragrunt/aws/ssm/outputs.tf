output "jwt_secret_key_arn" {
  description = "The SSM parameter ARN for the JWT secret key"
  value       = aws_ssm_parameter.jwt_secret_key.arn
}