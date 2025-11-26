 resource "aws_route53_zone" "feedback_viewer" {
  name = var.domain

  tags = {
    Name       = "${var.product_name}-zone"
    CostCentre = var.billing_code
    Terraform  = true
  }
}