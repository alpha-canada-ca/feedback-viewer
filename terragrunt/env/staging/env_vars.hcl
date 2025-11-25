inputs = {
  account_id       = "992382783569"
  env              = "staging"
  cost_center_code = "feedback-viewer-staging"
  region           = "ca-central-1"
  product_name     = "feedback-viewer"
  domain           = "feedback-viewer.cdssandbox.xyz"

  # dto-feedback-cj infrastructure references (must match their staging environment)
  # dto-feedback-cj staging infrastructure (from terragrunt output)
  dto_feedback_cj_vpc_id                 = "vpc-0936ad5356ea0fe6d"
  dto_feedback_cj_vpc_private_subnet_ids = ["subnet-089ba77b72ba4e7ef", "subnet-08d75a1492fee6cbd"]
  dto_feedback_cj_vpc_cidr_block         = "10.0.0.0/16"
  dto_feedback_cj_docdb_endpoint         = "feedback-cronjob-docdb-cluster.cluster-c52kwceay79r.ca-central-1.docdb.amazonaws.com"
  dto_feedback_cj_docdb_username_arn     = "arn:aws:ssm:ca-central-1:992382783569:parameter/feedback-cronjob/staging/docdb-username"
  dto_feedback_cj_docdb_password_arn     = "arn:aws:ssm:ca-central-1:992382783569:parameter/feedback-cronjob/staging/docdb-password"
}

