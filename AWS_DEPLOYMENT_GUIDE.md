# AWS Elastic Beanstalk Deployment Guide

Your fullstack todo app will be deployed to AWS Elastic Beanstalk with auto-scaling, load balancing, and managed infrastructure.

## Prerequisites Setup

### Step 1: Create AWS Access Keys

1. **Log in to AWS Console**: https://console.aws.amazon.com/
2. Click on your account name (top-right) → **Security Credentials**
3. Scroll down to **Access Keys** section
4. Click **Create access key**
   - Select "Command Line Interface (CLI)"
   - Accept the warning, click next
   - Copy and save both:
     - **Access Key ID** (looks like: `AKIA...`)
     - **Secret Access Key** (looks like: `wJal...`)
   - **IMPORTANT**: Save these somewhere secure - you won't see them again!

### Step 2: Install Required Tools

#### Option A: Using PowerShell (Recommended for Windows)

```powershell
# Install AWS CLI v2 using Chocolatey (if you have it)
choco install awscli

# Or download the MSI installer from:
# https://aws.amazon.com/cli/
```

#### Option B: Using Python/pip
```powershell
pip install awscli
```

**Verify installation:**
```powershell
aws --version
```

#### Install Elastic Beanstalk CLI
```powershell
pip install awsebcli
```

**Verify installation:**
```powershell
eb --version
```

### Step 3: Configure AWS Credentials

Run this command in PowerShell:
```powershell
aws configure
```

When prompted, enter:
- **AWS Access Key ID**: Your Access Key from Step 1
- **AWS Secret Access Key**: Your Secret Access Key from Step 1
- **Default region name**: Enter `us-east-1` (or your preferred region)
- **Default output format**: Press Enter to leave blank (or type `json`)

**Verify configuration:**
```powershell
aws sts get-caller-identity
```

Should output your AWS account info if credentials are correct.

---

## Deployment Steps

### Step 4: Build Your Application

Navigate to your project directory and build the JAR:

```powershell
cd "C:\Users\Jose\fullstack-todo-app\todo-app-rest"
mvn clean package
```

This creates: `target\fullstack-todo-app-1.0.0.jar`

### Step 5: Initialize Elastic Beanstalk

```powershell
cd "C:\Users\Jose\fullstack-todo-app\todo-app-rest"
eb init
```

When prompted:
- **Select application name**: Enter `todo-app` (or your choice)
- **Select platform**: Choose `Java` → `Corretto 25` (or latest Java version available)
- **Create CodeCommit repo**: Type `n` (no, we're not using CodeCommit)

### Step 6: Create Elastic Beanstalk Environment

```powershell
eb create todo-app-env `
  --envtype load-balanced `
  --instance-type t3.micro `
  --single
```

**Options explained:**
- `--envtype load-balanced`: Includes load balancer and auto-scaling
- `--instance-type t3.micro`: Free tier eligible
- `--single`: Single instance (remove for auto-scaling group)

### Step 7: Deploy Your Application

```powershell
eb deploy
```

Monitor deployment progress - this typically takes 3-5 minutes.

### Step 8: Verify Deployment

Check status and get your application URL:
```powershell
eb status
```

Open your application:
```powershell
eb open
```

This opens your app in the default browser at the Elastic Beanstalk URL.

---

## Configuration Files

### Amazon Cognito sign-in

The app uses Spring Security's OAuth 2.0 authorization-code flow. In the Cognito app client:

1. Enable the authorization-code grant and the `openid`, `email`, and `phone` scopes.
2. Enable self-service sign-up if users should be able to create accounts. The app's sign-in link opens Cognito's managed login page, which provides the sign-up option.
3. Add `https://<app-host>/login/oauth2/code/cognito` as an allowed callback URL. For local development, use `http://localhost:5000/login/oauth2/code/cognito`.
4. Add the app's root URL (for example, `https://<app-host>/`) as an allowed sign-out URL.
5. Set the Cognito app client domain and configure these environment variables for the application:

   - `COGNITO_CLIENT_ID`
   - `COGNITO_CLIENT_SECRET` (keep it out of source control)
   - `COGNITO_ISSUER_URI` (for example, `https://cognito-idp.us-east-1.amazonaws.com/<user-pool-id>`)
   - `COGNITO_DOMAIN` (the HTTPS Cognito managed-login domain, without a path)
   - `COGNITO_LOGOUT_URI` (the exact allowed sign-out URL, including its trailing slash if configured that way)

For local development, the values from the provided Cognito instructions are:

```powershell
$env:COGNITO_CLIENT_ID = '3slscn73sf9gfperki4paeda9'
$env:COGNITO_ISSUER_URI = 'https://cognito-idp.us-east-1.amazonaws.com/us-east-1_YHEz7v3CD'
$env:COGNITO_DOMAIN = 'https://<your-cognito-domain>.auth.us-east-1.amazoncognito.com'
$env:COGNITO_LOGOUT_URI = 'http://localhost:5000/'
```

Set `COGNITO_CLIENT_SECRET` securely in the current environment without committing it, then start the app from `todo-app-rest` with `mvn spring-boot:run`. For Elastic Beanstalk, configure these as environment properties and store the client secret in a protected secret store. The OAuth callback must target the Spring Boot application host, not a separate static-site hostname.

The todo API requires an authenticated Cognito session, uses CSRF protection for state-changing requests, and keeps each signed-in user's in-memory todo list separate. Todo data is still lost when the application restarts.

### Create `.ebignore` (in project root)

Tells EB what to ignore (similar to .gitignore):
```
node_modules/
.git/
.vscode/
*.md
AWS_DEPLOYMENT_GUIDE.md
```

Save as: `C:\Users\Jose\fullstack-todo-app\todo-app-rest\.ebignore`

### Configure Application Properties

Create or update: `C:\Users\Jose\fullstack-todo-app\todo-app-rest\src\main\resources\application.properties`

```properties
# Server Configuration
server.port=5000
server.servlet.context-path=/
spring.mvc.static-path-pattern=/static/**
spring.web.resources.static-locations=classpath:/public/

# Logging
logging.level.root=INFO
logging.level.com.example.todo=DEBUG

# Environment-specific
spring.profiles.active=${SPRING_PROFILES_ACTIVE:default}
```

### Create `application-production.properties`

Create: `C:\Users\Jose\fullstack-todo-app\todo-app-rest\src\main\resources\application-production.properties`

```properties
# Production settings
server.compression.enabled=true
server.compression.min-response-size=1024
logging.level.root=WARN
```

---

## Important Commands Reference

```powershell
# View environment status and URL
eb status

# Open app in browser
eb open

# View logs
eb logs

# SSH into instance
eb ssh

# Scale environment
eb scale 3  # Creates 3 instances

# Terminate environment (DELETE APP)
eb terminate todo-app-env

# View all environments
eb list

# Switch to different environment
eb use todo-app-env-2

# Deploy latest build
eb deploy

# Create new environment
eb create my-env --instance-type t3.micro
```

---

## Troubleshooting

### Deploy fails with "JAR not found"
- Ensure you ran `mvn clean package` successfully
- JAR should be in `target\fullstack-todo-app-1.0.0.jar`

### "eb" command not found
```powershell
# Install EB CLI
pip install awsebcli --upgrade
```

### Application won't start
```powershell
# View logs
eb logs

# SSH to instance
eb ssh

# Check Spring Boot logs
tail -f /var/log/eb-engine.log
```

### Can't access the application
- Ensure Security Group allows HTTP (port 80)
- Wait 5 minutes for DNS propagation
- Check Elastic Beanstalk console for health status

---

## AWS Costs

**Free Tier (eligible for 12 months):**
- `t3.micro` instance (1 vCPU, 1GB RAM): Free
- Elastic Load Balancer: Charged (~$16/month)
- Data transfer: Free for first 15GB/month

**Total estimated cost: ~$16/month** after free tier expires.

To reduce costs:
- Use `--single` flag (no load balancer) = Free tier only
- Set instance type to `t2.micro` instead of `t3.micro`

---

## Next Steps

1. Create AWS Access Keys (IAM Console)
2. Install AWS CLI and EB CLI
3. Run `aws configure` with your credentials
4. Build the app: `mvn clean package`
5. Initialize EB: `eb init`
6. Create environment: `eb create todo-app-env`
7. Deploy: `eb deploy`
8. Open app: `eb open`

Good luck! 🚀
