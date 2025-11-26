# IAM Role definitions

# Policy for ECS task role
data "aws_iam_policy_document" "feedback_viewer_ecs_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

data "aws_iam_policy_document" "feedback_viewer_ssm_policy" {
  statement {
    sid    = "AllowSSMParameterAccess"
    effect = "Allow"
    actions = [
      "ssm:GetParameter",
      "ssm:GetParameters",
      "ssm:GetParametersByPath"
    ]
    resources = [
      var.docdb_username_arn,
      var.docdb_password_arn,
      var.jwt_secret_key_arn
    ]
  }
}

resource "aws_iam_policy" "feedback_viewer_ssm_policy" {
  name        = "${var.product_name}-ssm-policy"
  description = "Policy for ${var.product_name} ${var.env} to access SSM parameters"
  policy      = data.aws_iam_policy_document.feedback_viewer_ssm_policy.json

  tags = {
    CostCentre = var.billing_code
    Terraform  = true
  }
}

resource "aws_iam_role" "feedback_viewer_ecs_role" {
  name               = "${var.product_name}-ecs-role"
  assume_role_policy = data.aws_iam_policy_document.feedback_viewer_ecs_policy.json
}

resource "aws_iam_role_policy_attachment" "feedback_viewer_ecs_policy" {
  role       = aws_iam_role.feedback_viewer_ecs_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy_attachment" "feedback_viewer_ecs_role_ssm_policy" {
  policy_arn = aws_iam_policy.feedback_viewer_ssm_policy.arn
  role       = aws_iam_role.feedback_viewer_ecs_role.name
}