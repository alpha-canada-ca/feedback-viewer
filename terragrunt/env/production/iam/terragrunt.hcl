terraform {
  source = "../../../aws//iam"
}

dependency "ssm" {
  config_path                             = "../ssm"
  mock_outputs_allowed_terraform_commands = ["init", "fmt", "validate", "plan", "show"]
  mock_outputs_merge_with_state           = true
  mock_outputs = {
    docdb_username_arn = "arn:aws:ssm:ca-central-1:123456789012:parameter/mock_docdb_username"
    docdb_password_arn = "arn:aws:ssm:ca-central-1:123456789012:parameter/mock_docdb_password"
    jwt_secret_key_arn = "arn:aws:ssm:ca-central-1:123456789012:parameter/mock_jwt_secret_key"
  }
}

inputs = {
  docdb_username_arn = dependency.ssm.outputs.docdb_username_arn
  docdb_password_arn = dependency.ssm.outputs.docdb_password_arn
  jwt_secret_key_arn = dependency.ssm.outputs.jwt_secret_key_arn
}

include {
  path = find_in_parent_folders("root.hcl")
}