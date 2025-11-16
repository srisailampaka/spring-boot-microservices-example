# Generates a self-signed PKCS12 keystore and moves it to src/main/resources
# Requires keytool (JDK) available in PATH.
Param(
    [string]$Password = 'changeit',
    [string]$Alias = 'auth-keystore',
    [string]$Output = 'auth-keystore.p12'
)

Write-Host "Generating keystore $Output..."
keytool -genkeypair -alias $Alias -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore $Output -validity 3650 -storepass $Password -keypass $Password -dname "CN=localhost"

$dest = Join-Path -Path (Resolve-Path .).Path -ChildPath 'src\main\resources\auth-keystore.p12'
Move-Item -Path $Output -Destination $dest -Force
Write-Host "Keystore moved to $dest"
