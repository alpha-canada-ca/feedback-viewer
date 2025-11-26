terraform {
  source = "../../../aws//ssm"
}

include {
  path = find_in_parent_folders("root.hcl")
}