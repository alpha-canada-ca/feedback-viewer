resource "aws_security_group" "feedback_viewer_load_balancer_sg" {
  name        = "feedback_viewer_load_balancer_sg"
  description = "Security group for load balancer"
  vpc_id      = var.vpc_id

  tags = merge(var.default_tags, {
    CostCentre = var.billing_code
  })
}

resource "aws_security_group_rule" "lb_ingress_https" {
  description       = "Allow HTTPS inbound traffic"
  type              = "ingress"
  from_port         = 443
  to_port           = 443
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.feedback_viewer_load_balancer_sg.id
}

resource "aws_security_group_rule" "lb_egress_ecs" {
  description       = "Allow load balancer to send traffic to ECS tasks on port 3001"
  type              = "egress"
  from_port         = 3001
  to_port           = 3001
  protocol          = "tcp"
  cidr_blocks       = [var.vpc_cidr_block]
  security_group_id = aws_security_group.feedback_viewer_load_balancer_sg.id
}