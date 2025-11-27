resource "aws_route53_record" "feedback_viewer" {
  zone_id = var.hosted_zone_id
  name    = var.domain
  type    = "A"

  alias {
    name                   = aws_lb.feedback_viewer.dns_name
    zone_id                = aws_lb.feedback_viewer.zone_id
    evaluate_target_health = false
  }
}
