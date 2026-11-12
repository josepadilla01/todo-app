# 🚀 AWS Deployment Guide for Your Todo App

This guide will help you deploy your fullstack todo app to AWS Elastic Beanstalk in minutes.

## What You're Deploying

- **Backend**: Spring Boot REST API (Java 25)
- **Frontend**: Static HTML/CSS/JS served from Spring Boot
- **Deployment Target**: AWS Elastic Beanstalk (managed, auto-scaling, load-balanced)

## Prerequisites (First Time Only)

### 1. Create AWS Account & Access Keys

1. Go to https://aws.amazon.com/
2. Create a free tier account (eligible for 12 months free)
3. Log into AWS Console
4. Click your name (top-right) → **Security Credentials**
5. Under "Access Keys", click **Create access key**
   - Purpose: CLI
   - Note your **Access Key ID** (looks like `AKIA...`)
   - Note your **Secret Access Key** (looks like `wJal...`)
   - ⚠️ **SAVE THESE** - you can't see them again!

### 2. Install Tools

**AWS CLI (official tool)**
- Download MSI from https://aws.amazon.com/cli/
- Or run: `choco install awscli` (if you have Chocolatey)
- Verify: `aws --version`

**Elastic Beanstalk CLI (EB CLI)**
```powershell
pip install awsebcli
eb --version  # Verify
```

**Other Tools** (if not already installed)
- Java: `java -version`
- Maven: `mvn --version`

### 3. Configure the app login

Set `TODO_APP_USERNAME` and `TODO_APP_PASSWORD` in the environment where the
app runs. Both values are required and must be nonblank. For local PowerShell
sessions, set them before starting the app:

```powershell
$env:TODO_APP_USERNAME = Read-Host "App username"
$env:TODO_APP_PASSWORD = Read-Host "App password"
```

Do not commit real credentials. Configure these variables separately in your
deployment environment as well. This login is a demo UI check: the todo API
does not enforce authentication and should not be exposed as a protected service.

### 4. Configure AWS Credentials

```powershell
aws configure
```

When prompted, enter:
- **Access Key ID**: From step 1
- **Secret Access Key**: From step 1  
- **Default region**: `us-east-1` (or your choice)
- **Default output format**: Press Enter (or type `json`)

**Verify it works:**
```powershell
aws sts get-caller-identity
```

Should show your AWS account information.

---

## Quick Deployment (3 Steps)

### Step 1: Build Your App

```powershell
cd C:\Users\Jose\fullstack-todo-app\todo-app-rest
mvn clean package
```

Should show `BUILD SUCCESS` and create `target\fullstack-todo-app-1.0.0.jar`

### Step 2: Initialize Elastic Beanstalk

```powershell
eb init
```

Respond to prompts:
- Application name: `todo-app`
- Platform: Choose `Java` → `Corretto 25`
- CodeCommit: Type `n`

This creates `.elasticbeanstalk\config.yml`

### Step 3: Create & Deploy

```powershell
# Create environment (one-time)
eb create todo-app-env --instance-type t3.micro --single

# Deploy updates (any time)
eb deploy
```

**That's it!** Your app is live in 5-10 minutes.

To open it:
```powershell
eb open
```

---

## Automated Deployment Script

### Using PowerShell

```powershell
# First time (creates environment)
.\deploy.ps1 -Action create

# Subsequent deployments
.\deploy.ps1 -Action deploy

# Check status
.\deploy.ps1 -Action status

# View logs
.\deploy.ps1 -Action logs

# Open in browser
.\deploy.ps1 -Action open
```

### Using Batch Script

```cmd
# First deployment
deploy.bat
```

---

## Common Tasks

### Check Deployment Status
```powershell
eb status
```

### View Real-Time Logs
```powershell
eb logs
```

### SSH Into Server
```powershell
eb ssh
# Then: tail -f /var/log/eb-engine.log
```

### Scale to Multiple Instances
```powershell
eb scale 2  # Creates 2 instances with load balancer
```

### Deploy New Version
```powershell
mvn clean package
eb deploy
```

### Delete Everything (Stop Costs)
```powershell
eb terminate todo-app-env
```

---

## Understanding EB Environment Options

### Free Tier Configuration (Recommended for Testing)
```powershell
eb create todo-app-env `
  --instance-type t3.micro `
  --single
```
- **Cost**: Free for 12 months (t3.micro eligible)
- **Instances**: 1
- **Load Balancer**: None
- **Best for**: Development, testing, light traffic

### Production Configuration
```powershell
eb create todo-app-prod `
  --instance-type t3.small `
  --scale 2
```
- **Cost**: ~$30-50/month after free tier
- **Instances**: 2 (auto-scales up to 6)
- **Load Balancer**: Included (~$16/month)
- **Best for**: Production, high availability

### Custom Configuration
Edit `.elasticbeanstalk\config.yml`:
```yaml
environments:
  todo-app-env:
    instance_type: t3.micro
```

---

## Cost Breakdown

### Free Tier (First 12 Months)
- EC2 t3.micro: 750 hours/month ✓ FREE
- Data transfer: First 15GB/month ✓ FREE
- Total: **$0/month**

### After Free Tier (with --single flag)
- EC2 t3.micro: ~$7/month
- Data transfer: Included
- Total: **~$7/month**

### With Load Balancer (without --single)
- EC2 t2.micro: ~$7/month
- Application Load Balancer: ~$16/month
- Data transfer: Included
- Total: **~$23/month**

### To Keep Costs at Zero
1. Use `--single` flag (no load balancer)
2. Use `t3.micro` instance type
3. Keep within free tier limits
4. Delete with `eb terminate` when done

---

## Troubleshooting

### "AWS CLI not found"
- Download: https://aws.amazon.com/cli/
- Restart PowerShell after installation

### "eb command not found"
```powershell
pip install awsebcli --upgrade
```

### "AWS credentials not configured"
```powershell
aws configure
# Enter your Access Key ID and Secret Access Key
```

### Build fails
```powershell
# Check for Java errors
mvn clean
mvn package -X  # Verbose output

# Common issues
# - Java version mismatch (app uses Java 25)
# - Missing dependencies (run mvn again)
```

### App deployed but won't start
```powershell
# Check logs
eb logs

# SSH to instance
eb ssh
# View Spring Boot logs
tail -f /var/log/eb-engine.log
```

### Can't access app URL
- Wait 5 minutes for DNS propagation
- Check AWS console for health status
- Try: `eb open` (refreshes URL)
- Check Security Group allows port 80

### Deployment hangs
```powershell
# Press Ctrl+C to stop
# Then check logs
eb logs

# Or check EC2 console for errors
```

---

## Project Structure

```
fullstack-todo-app/
├── todo-app-rest/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/todo/
│   │       │   └── TodoApplication.java
│   │       └── resources/
│   │           ├── application.properties        # ← Configuration
│   │           └── application-production.properties
│   ├── .ebextensions/
│   │   └── java.config                          # ← EB configuration
│   ├── .ebignore                                # ← What to ignore during deploy
│   ├── pom.xml                                  # ← Build configuration
│   ├── deploy.bat                               # ← Batch deployment script
│   └── deploy.ps1                               # ← PowerShell script
├── public/                                       # ← Static frontend files
│   ├── index.html
│   ├── app.js
│   └── styles.css
├── AWS_DEPLOYMENT_GUIDE.md                      # ← Detailed guide
└── DEPLOYMENT_CHECKLIST.md                      # ← Step-by-step checklist
```

---

## Application Configuration

### Production Settings
- Log level: WARN (less verbose)
- Compression: Enabled
- Error details: Hidden
- Profile: `production`

To deploy in production mode:
```powershell
eb setenv SPRING_PROFILES_ACTIVE=production
eb deploy
```

---

## Next Steps

1. **Create AWS Access Keys** (if you haven't)
2. **Install AWS CLI & EB CLI** (follow Prerequisites)
3. **Run deployment** (Quick Deployment section)
4. **Test your app** (open in browser)
5. **Monitor with logs** (`eb logs`)
6. **Set up auto-scaling** (optional, for production)

---

## Support & Resources

- **AWS Elastic Beanstalk Docs**: https://docs.aws.amazon.com/elasticbeanstalk/
- **Spring Boot on AWS**: https://spring.io/guides/gs/deploying-spring-boot-app-with-aws/
- **AWS Pricing Calculator**: https://calculator.aws/
- **EB CLI Command Reference**: https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb-cli3.html

---

## Security Tips

1. **Never commit credentials** to git (they're in `.gitignore`)
2. **Rotate access keys** regularly
3. **Use IAM roles** instead of long-term credentials
4. **Enable MFA** on your AWS account
5. **Monitor costs** with AWS Budgets
6. **Use application logging** for debugging (don't log credentials)

---

**Happy deploying! 🚀**

Questions? Check the `DEPLOYMENT_CHECKLIST.md` or the detailed `AWS_DEPLOYMENT_GUIDE.md`
