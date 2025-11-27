terraform {
  source = "../../../aws//iam"
}

dependency "ssm" {
  config_path                             = "../ssm"
  mock_outputs_allowed_terraform_commands = ["init", "fmt", "validate", "plan", "show"]
  mock_outputs_merge_with_state           = true
  mock_outputs = {
    jwt_secret_key_arn = ""
  }
}

locals {
  env_vars = read_terragrunt_config(find_in_parent_folders("env_vars.hcl"))
}

inputs = {
  # DocumentDB credentials from dto-feedback-cj (via env_vars.hcl)
  docdb_username_arn = local.env_vars.inputs.dto_feedback_cj_docdb_username_arn
  docdb_password_arn = local.env_vars.inputs.dto_feedback_cj_docdb_password_arn

  # JWT secret from our SSM module
  jwt_secret_key_arn = dependency.ssm.outputs.jwt_secret_key_arn
}

include {
  path = find_in_parent_folders("root.hcl")
}