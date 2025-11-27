resource "aws_acm_certificate" "feedback_viewer" {
  domain_name       = var.domain
  validation_method = "DNS"

  tags = merge(var.default_tags, {
    Name       = "${var.product_name}-cert"
    CostCentre = var.billing_code
    Terraform  = true
  })

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_route53_record" "feedback_viewer_certificate_validation" {
  for_each = {
    for dvo in aws_acm_certificate.feedback_viewer.domain_validation_options : dvo.domain_name => {
      name    = dvo.resource_record_name
      type    = dvo.resource_record_type
      record  = dvo.resource_record_value
      zone_id = var.hosted_zone_id
    }
  }

  allow_overwrite = true
  name            = each.value.name
  records         = [each.value.record]
  type            = each.value.type
  zone_id         = each.value.zone_id
  ttl             = 60
}

resource "aws_acm_certificate_validation" "feedback_viewer" {
  certificate_arn         = aws_acm_certificate.feedback_viewer.arn
  validation_record_fqdns = [for record in aws_route53_record.feedback_viewer_certificate_validation : record.fqdn]
}
