data_dir = "/tmp/"

ports {
  grpc = 8502
}

server = true

bootstrap_expect = 1

ui = true

bind_addr = "0.0.0.0"

client_addr = "0.0.0.0"

enable_central_service_config = true

connect {
  enabled = true
}

ui_config {
  enabled = true
  metrics_provider = "prometheus"
 
  metrics_proxy {
    base_url = "http://host.docker.internal:9090"
  }
  dashboard_url_templates {
  # Resolution here occurs from the client level (i.e. browser)
    service = "http://localhost:3000/explore?orgId=1&left=%5B%22now-1h%22,%22now%22,%22loki%22,%7B%22expr%22:%22%7Bservice_name%3D%5C%22{{Service.Name}}%5C%22%7D%22%7D%5D"
  }
}
