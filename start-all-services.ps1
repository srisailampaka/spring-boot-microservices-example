# PowerShell script to start all microservices in correct order
$projectRoot = "d:\Srisailam\Java\spring-boot-microservices-netflix-eureka-service-registry-example"
$maven = "c:\Program Files\Apache Software Foundation\apache-maven-3.9.11\bin\mvn.cmd"

Write-Host "Starting microservices..." -ForegroundColor Green

# Step 1: Clean and build all projects
Write-Host "Building all projects..." -ForegroundColor Yellow
Set-Location "$projectRoot\eureka-service-registry"
& $maven clean package -DskipTests
Set-Location "$projectRoot\auth-service"
& $maven clean package -DskipTests
Set-Location "$projectRoot\service-1"
& $maven clean package -DskipTests
Set-Location "$projectRoot\service-2"
& $maven clean package -DskipTests
Set-Location "$projectRoot\api-gateway"
& $maven clean package -DskipTests

Write-Host "All projects built successfully!" -ForegroundColor Green

Write-Host "`nStarting services in order..." -ForegroundColor Yellow

# Step 2: Start Eureka Service Registry (port 8761)
Write-Host "1. Starting Eureka Service Registry on port 8761..." -ForegroundColor Cyan
Set-Location "$projectRoot\eureka-service-registry"
Start-Process -FilePath $maven -ArgumentList "spring-boot:run" -WindowStyle Normal

Start-Sleep -Seconds 30  # Wait for Eureka to start

# Step 3: Start Auth Service (port 8090)
Write-Host "2. Starting Auth Service on port 8090..." -ForegroundColor Cyan
Set-Location "$projectRoot\auth-service"
Start-Process -FilePath $maven -ArgumentList "spring-boot:run" -WindowStyle Normal

Start-Sleep -Seconds 20

# Step 4: Start Service-1 (port 8084)
Write-Host "3. Starting Service-1 on port 8084..." -ForegroundColor Cyan
Set-Location "$projectRoot\service-1"
Start-Process -FilePath $maven -ArgumentList "spring-boot:run" -WindowStyle Normal

Start-Sleep -Seconds 15

# Step 5: Start Service-2 (port 8085)
Write-Host "4. Starting Service-2 on port 8085..." -ForegroundColor Cyan
Set-Location "$projectRoot\service-2"
Start-Process -FilePath $maven -ArgumentList "spring-boot:run" -WindowStyle Normal

Start-Sleep -Seconds 15

# Step 6: Start API Gateway (port 8080)
Write-Host "5. Starting API Gateway on port 8080..." -ForegroundColor Cyan
Set-Location "$projectRoot\api-gateway"
Start-Process -FilePath $maven -ArgumentList "spring-boot:run" -WindowStyle Normal

Write-Host "`nAll services are starting up!" -ForegroundColor Green
Write-Host "Services will be available at:" -ForegroundColor White
Write-Host "- Eureka Dashboard: http://localhost:8761" -ForegroundColor Yellow
Write-Host "- Auth Service: https://localhost:8090" -ForegroundColor Yellow
Write-Host "- Service-1: http://localhost:8084" -ForegroundColor Yellow
Write-Host "- Service-2: http://localhost:8085" -ForegroundColor Yellow
Write-Host "- API Gateway: http://localhost:8080" -ForegroundColor Yellow

Write-Host "`nWait a few minutes for all services to register with Eureka..." -ForegroundColor Magenta
Write-Host "Then test with: curl http://localhost:8080/service-1/hello" -ForegroundColor Magenta