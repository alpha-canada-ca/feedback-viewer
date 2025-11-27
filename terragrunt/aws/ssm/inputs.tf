
variable "jwt_secret_key" {
  description = "The secret key used to sign JWT tokens"
  sensitive   = true
  type        = string
}