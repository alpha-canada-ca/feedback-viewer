terraform {
  source = "../../../aws//load_balancer"
}

dependency "hosted_zone" {
  config_path                             = "../hosted_zone"
  mock_outputs_allowed_terraform_commands = ["init", "fmt", "validate", "plan", "show"]
  mock_outputs_merge_with_state           = true
  mock_outputs = {
    hosted_zone_id = "Z1234567890ABC"
  }
}

inputs = {
  # VPC configuration from dto-feedback-cj infrastructure
  vpc_id                = local.vars.inputs.dto_feedback_cj_vpc_id
  vpc_cidr_block        = local.vars.inputs.dto_feedback_cj_vpc_cidr_block
  vpc_public_subnet_ids = local.vars.inputs.dto_feedback_cj_vpc_public_subnet_ids

  # DNS configuration
  hosted_zone_id = dependency.hosted_zone.outputs.hosted_zone_id
}

locals {
  vars = read_terragrunt_config(find_in_parent_folders("env_vars.hcl"))
}

include {
  path = find_in_parent_folders("root.hcl")
}
