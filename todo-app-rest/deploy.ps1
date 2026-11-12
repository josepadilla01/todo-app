# AWS Elastic Beanstalk Deployment Script for PowerShell
# Usage: .\deploy.ps1
# Description: Builds and deploys your todo app to AWS Elastic Beanstalk

param(
    [ValidateSet("create", "deploy", "logs", "status", "open", "terminate")]
    [string]$Action = "deploy",
    [string]$Environment = "todo-app-env"
)

function Write-Header {
    param([string]$Text)
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Cyan
    Write-Host "  $Text" -ForegroundColor Cyan
    Write-Host "============================================" -ForegroundColor Cyan
    Write-Host ""
}

function Test-Tool {
    param([string]$Command, [string]$Name)
    try {
        $output = Invoke-Expression $Command 2>&1
        return $true
    } catch {
        return $false
    }
}

Write-Header "Todo App - AWS Elastic Beanstalk Deployment"

# Check if we're in the right directory
if (!(Test-Path "pom.xml")) {
    Write-Host "ERROR: pom.xml not found!" -ForegroundColor Red
    Write-Host "Please run this script from the todo-app-rest directory." -ForegroundColor Red
    exit 1
}

# Check prerequisites
Write-Host "[*] Checking prerequisites..." -ForegroundColor Yellow

if (!(Test-Tool "aws --version" "AWS CLI")) {
    Write-Host "ERROR: AWS CLI not installed!" -ForegroundColor Red
    Write-Host "Download from: https://aws.amazon.com/cli/" -ForegroundColor Yellow
    exit 1
}
Write-Host "[OK] AWS CLI found" -ForegroundColor Green

if (!(Test-Tool "eb --version" "EB CLI")) {
    Write-Host "ERROR: EB CLI not installed!" -ForegroundColor Red
    Write-Host "Install with: pip install awsebcli" -ForegroundColor Yellow
    exit 1
}
Write-Host "[OK] EB CLI found" -ForegroundColor Green

if (!(Test-Tool "java -version" "Java")) {
    Write-Host "ERROR: Java not installed!" -ForegroundColor Red
    exit 1
}
Write-Host "[OK] Java found" -ForegroundColor Green

if (!(Test-Tool "mvn --version" "Maven")) {
    Write-Host "ERROR: Maven not installed!" -ForegroundColor Red
    exit 1
}
Write-Host "[OK] Maven found" -ForegroundColor Green

# Check AWS credentials
Write-Host "[*] Verifying AWS credentials..." -ForegroundColor Yellow
try {
    $identity = aws sts get-caller-identity 2>&1 | ConvertFrom-Json
    Write-Host "[OK] AWS credentials configured" -ForegroundColor Green
    Write-Host "    Account: $($identity.Account)" -ForegroundColor Gray
    Write-Host "    User: $($identity.Arn)" -ForegroundColor Gray
} catch {
    Write-Host "ERROR: AWS credentials not configured!" -ForegroundColor Red
    Write-Host "Run: aws configure" -ForegroundColor Yellow
    exit 1
}

# Execute requested action
switch ($Action) {
    "create" {
        Write-Header "Creating Elastic Beanstalk Environment"
        Write-Host "[*] Building application..." -ForegroundColor Yellow
        mvn clean package
        if ($LASTEXITCODE -ne 0) {
            Write-Host "ERROR: Build failed!" -ForegroundColor Red
            exit 1
        }
        Write-Host "[OK] Build successful" -ForegroundColor Green

        Write-Host "[*] Initializing Elastic Beanstalk..." -ForegroundColor Yellow
        if (!(Test-Path ".elasticbeanstalk\config.yml") -or ((Get-Content ".elasticbeanstalk\config.yml" | Select-String "environment:.*null").Length -gt 0)) {
            Write-Host "[*] Running eb init..." -ForegroundColor Yellow
            # Provide responses to eb init prompts: select region (1 for us-east-1), select platform (8 for Java)
            $initResponses = @"
1
8
n
"@
            $initResponses | & eb init -a todo-app 2>&1 | Out-Null
            Write-Host "[OK] EB initialized" -ForegroundColor Green
        } else {
            Write-Host "[OK] EB already initialized" -ForegroundColor Green
        }

        Write-Host "[*] Creating environment '$Environment'..." -ForegroundColor Yellow
        Write-Host "[*] This may take several minutes..." -ForegroundColor Yellow
        
        # Attempt to create the environment
        & eb create $Environment --instance-type t3.micro --single 2>&1
        
        if ($LASTEXITCODE -eq 0 -or $LASTEXITCODE -eq 1) {
            Write-Host ""
            Write-Host "[OK] Environment creation initiated!" -ForegroundColor Green
            Write-Host ""
            Write-Host "Your app is being deployed. Monitoring status..." -ForegroundColor Cyan
            Start-Sleep -Seconds 5
            & eb status
            Write-Host ""
            Write-Host "Next steps:" -ForegroundColor Cyan
            Write-Host "  - Check deployment status: .\deploy.ps1 -Action status" -ForegroundColor Yellow
            Write-Host "  - View logs: .\deploy.ps1 -Action logs" -ForegroundColor Yellow
            Write-Host "  - Open in browser: .\deploy.ps1 -Action open" -ForegroundColor Yellow
        }
    }

    "deploy" {
        Write-Header "Deploying Application"
        Write-Host "[*] Building application..." -ForegroundColor Yellow
        mvn clean package
        if ($LASTEXITCODE -ne 0) {
            Write-Host "ERROR: Build failed!" -ForegroundColor Red
            exit 1
        }
        Write-Host "[OK] Build successful" -ForegroundColor Green

        Write-Host "[*] Deploying to EB..." -ForegroundColor Yellow
        eb deploy
        Write-Host "[OK] Deployment started" -ForegroundColor Green
        Write-Host ""
        Write-Host "Check status: eb status" -ForegroundColor Yellow
    }

    "logs" {
        Write-Header "Viewing Application Logs"
        eb logs
    }

    "status" {
        Write-Header "Environment Status"
        eb status
    }

    "open" {
        Write-Header "Opening Application"
        eb open
    }

    "terminate" {
        Write-Header "Terminating Environment"
        Write-Host "WARNING: This will delete '$Environment' and all its resources!" -ForegroundColor Red
        Write-Host "Cost will stop accruing immediately." -ForegroundColor Yellow
        $confirm = Read-Host "Type 'yes' to confirm"
        if ($confirm -eq "yes") {
            eb terminate $Environment
            Write-Host "[OK] Environment terminated" -ForegroundColor Green
        } else {
            Write-Host "[*] Cancelled" -ForegroundColor Yellow
        }
    }
}

Write-Host ""
Write-Host "Done!" -ForegroundColor Green
