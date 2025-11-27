terraform {
  source = "../../../aws//ecs"
}

dependencies {
  paths = ["../iam", "../ecr", "../load_balancer", "../ssm"]
}

dependency "iam" {
  config_path = "../iam"

  mock_outputs_allowed_terraform_commands = ["init", "fmt", "validate", "plan", "show"]
  mock_outputs_merge_with_state           = true
  mock_outputs = {
    iam_role_feedback-viewer-ecs-role_arn = ""
    feedback-viewer-ecs-policy_attachment = ""
  }
}


dependency "ecr" {
  config_path = "../ecr"

  mock_outputs_allowed_terraform_commands = ["init", "fmt", "validate", "plan", "show"]
  mock_outputs_merge_with_state           = true
  mock_outputs = {
    ecr_repository_arn = ""
    ecr_repository_url = ""
  }
}

dependency "load_balancer" {
  config_path = "../load_balancer"

  mock_outputs_allowed_terraform_commands = ["init", "fmt", "validate", "plan", "show"]
  mock_outputs_merge_with_state           = true
  mock_outputs = {
    lb_listener                      = ""
    lb_target_group_arn              = ""
    feedback_viewer_load_balancer_sg = ""
  }
}


dependency "ssm" {
  config_path = "../ssm"

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
  # From root.hcl (automatically inherited via include block)
  # product_name, billing_code, env are available

  # IAM
  iam_role_feedback-viewer-ecs-role_arn = dependency.iam.outputs.iam_role_feedback-viewer-ecs-role_arn
  feedback_viewer-ecs-policy_attachment = dependency.iam.outputs.feedback-viewer-ecs-policy_attachment

  # Network - from env_vars.hcl (dto-feedback-cj infrastructure)
  vpc_id             = local.env_vars.inputs.dto_feedback_cj_vpc_id
  vpc_cidr_block     = local.env_vars.inputs.dto_feedback_cj_vpc_cidr_block
  private_subnet_ids = local.env_vars.inputs.dto_feedback_cj_vpc_private_subnet_ids

  # Load balancer
  lb_listener          = dependency.load_balancer.outputs.lb_listener
  lb_target_group_arn  = dependency.load_balancer.outputs.lb_target_group_arn
  lb_security_group_id = dependency.load_balancer.outputs.feedback_viewer_load_balancer_sg

  # ECR
  ecr_repository_url = dependency.ecr.outputs.ecr_repository_url

  # DocumentDB - from env_vars.hcl (dto-feedback-cj infrastructure)
  docdb_endpoint     = local.env_vars.inputs.dto_feedback_cj_docdb_endpoint
  docdb_username_arn = local.env_vars.inputs.dto_feedback_cj_docdb_username_arn
  docdb_password_arn = local.env_vars.inputs.dto_feedback_cj_docdb_password_arn

  # Secrets
  jwt_secret_key_arn = dependency.ssm.outputs.jwt_secret_key_arn
}

include {
  path = find_in_parent_folders("root.hcl")
}
