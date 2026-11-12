@echo off
REM Quick deployment script for AWS Elastic Beanstalk
REM This script will build and deploy your todo app to AWS

echo.
echo ============================================
echo   Todo App - AWS Elastic Beanstalk Deploy
echo ============================================
echo.

REM Check if we're in the right directory
if not exist "pom.xml" (
    echo ERROR: pom.xml not found. Please run this script from the todo-app-rest directory.
    exit /b 1
)

REM Step 1: Check if tools are installed
echo [1/5] Checking prerequisites...
aws --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: AWS CLI not found. Please install it first.
    echo Visit: https://aws.amazon.com/cli/
    exit /b 1
)

eb --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: EB CLI not found. Please install it first.
    echo Run: pip install awsebcli
    exit /b 1
)

REM Step 2: Check AWS credentials
echo [2/5] Checking AWS credentials...
aws sts get-caller-identity >nul 2>&1
if errorlevel 1 (
    echo ERROR: AWS credentials not configured.
    echo Run: aws configure
    exit /b 1
)
echo AWS credentials OK

REM Step 3: Build the application
echo [3/5] Building application...
call mvn clean package
if errorlevel 1 (
    echo ERROR: Build failed
    exit /b 1
)
echo Build successful!

REM Step 4: Initialize EB (if needed)
echo [4/5] Initializing Elastic Beanstalk...
if not exist ".elasticbeanstalk\config.yml" (
    echo Running eb init...
    call eb init -p "Java 25" "todo-app"
)

REM Step 5: Deploy
echo [5/5] Deploying to AWS...
call eb deploy
if errorlevel 1 (
    echo ERROR: Deployment failed
    exit /b 1
)

echo.
echo ============================================
echo   Deployment Complete!
echo ============================================
echo.
echo Your app is deployed! To open it, run:
echo   eb open
echo.
echo Other useful commands:
echo   eb status     - Check environment status
echo   eb logs       - View application logs
echo   eb ssh        - SSH into an instance
echo   eb terminate  - Delete the environment
echo.
