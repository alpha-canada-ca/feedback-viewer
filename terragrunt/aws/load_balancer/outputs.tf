output "lb_listener" {
  description = "Load balancer listener of Feedback Viewer"
  value       = aws_lb_listener.feedback_viewer_listener
}

output "lb_target_group_arn" {
  description = "Arn of the Load balancer target group"
  value       = aws_lb_target_group.feedback_viewer.arn
}

output "feedback_viewer_load_balancer_sg" {
  description = "Security group of the Load balancer"
  value       = aws_security_group.feedback_viewer_load_balancer_sg.id
}