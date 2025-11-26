
variable "docdb_username" {
  description = "The username of the documentdb cluseter"
  sensitive   = true
  type        = string
}

variable "docdb_password" {
  description = "The password of the documentdb cluster"
  sensitive   = true
}

variable "jwt_secret_key" {
  description = "The secret key used to sign JWT tokens"
  sensitive   = true
  type        = string
}