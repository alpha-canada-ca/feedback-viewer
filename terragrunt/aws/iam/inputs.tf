variable "product_name" {
  description = "(Required) The name of the product you are deploying."
  type        = string
}

variable "env" {
  description = "The current running environment"
  type        = string
}

variable "billing_code" {
  description = "The billing code to tag our resources with"
  type        = string
}

variable "default_tags" {
  description = "The default tags we apply to all resources"
  type        = map(string)
}

variable "docdb_username_arn" {
  description = "The ARN of the DocumentDB username SSM parameter"
  type        = string
}

variable "docdb_password_arn" {
  description = "The ARN of the DocumentDB password SSM parameter"
  type        = string
}

variable "jwt_secret_key_arn" {
  description = "The ARN of the JWT secret key SSM parameter"
  type        = string
}