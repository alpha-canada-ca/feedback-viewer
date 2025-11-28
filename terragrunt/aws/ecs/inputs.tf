variable "vpc_id" {
  description = "VPC ID where ECS tasks will run"
  type        = string
}

variable "vpc_cidr_block" {
  description = "CIDR block of the VPC"
  type        = string
}

variable "private_subnet_ids" {
  description = "Private subnet IDs for ECS tasks"
  type        = list(string)
}

variable "iam_role_arn" {
  description = "IAM role ARN for ECS tasks"
  type        = string
}

variable "lb_target_group_arn" {
  description = "Load balancer target group ARN"
  type        = string
}

variable "lb_security_group_id" {
  description = "Load balancer security group ID"
  type        = string
}

variable "ecr_repository_url" {
  description = "ECR repository URL for container image"
  type        = string
}

variable "image_tag" {
  description = "Container image tag to deploy"
  type        = string
  default     = "latest"
}

variable "container_port" {
  description = "Port the container listens on"
  type        = number
  default     = 3001
}

variable "cpu" {
  description = "Fargate task CPU units"
  type        = number
  default     = 1024
}

variable "memory" {
  description = "Fargate task memory in MB"
  type        = number
  default     = 4096
}

variable "desired_count" {
  description = "Desired number of ECS tasks"
  type        = number
  default     = 1
}

variable "docdb_username_arn" {
  description = "SSM parameter ARN for DocumentDB username"
  type        = string
}

variable "docdb_password_arn" {
  description = "SSM parameter ARN for DocumentDB password"
  type        = string
}

variable "docdb_endpoint" {
  description = "DocumentDB cluster endpoint"
  type        = string
}

variable "jwt_secret_key_arn" {
  description = "SSM parameter ARN for JWT secret key"
  type        = string
}