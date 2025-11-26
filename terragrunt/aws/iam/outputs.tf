output "feedback_viewer_ecs_role_arn" {
  description = "The ARN of the ECS task execution role"
  value       = aws_iam_role.feedback_viewer_ecs_role.arn
}