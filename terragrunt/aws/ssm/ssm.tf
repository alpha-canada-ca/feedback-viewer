# DocumentDB credentials are managed in dto-feedback-cj repo
# Reference them via env_vars.hcl: dto_feedback_cj_docdb_username_arn and dto_feedback_cj_docdb_password_arn

resource "aws_ssm_parameter" "jwt_secret_key" {
  name  = "/feedback-viewer/staging/jwt_secret_key"
  type  = "SecureString"
  value = var.jwt_secret_key

  tags = {
    CostCentre = var.billing_code
    Terraform  = true
  }
}