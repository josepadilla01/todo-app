# 🚀 DEPLOYMENT QUICK REFERENCE CARD

## Pre-Deployment Checklist (One-Time)

- [ ] Create AWS Account at https://aws.amazon.com (free tier)
- [ ] Create IAM Access Keys in AWS Console
- [ ] Install AWS CLI: https://aws.amazon.com/cli/
- [ ] Install EB CLI: `pip install awsebcli`
- [ ] Run: `aws configure` and enter your credentials
- [ ] Verify: `aws sts get-caller-identity`

**⏱️ Time to complete: ~15 minutes**

---

## Deployment Command (Copy & Paste)

### Option A: Using PowerShell Script (EASIEST)

```powershell
cd C:\Users\Jose\fullstack-todo-app\todo-app-rest
.\deploy.ps1 -Action create      # First time only
.\deploy.ps1 -Action deploy      # For updates
.\deploy.ps1 -Action open        # Open in browser
```

### Option B: Using Commands Directly

```powershell
cd C:\Users\Jose\fullstack-todo-app\todo-app-rest

# First time setup
mvn clean package
eb init
eb create todo-app-env --instance-type t3.micro --single

# Deploy
eb deploy
eb open
```

**⏱️ Time to deploy: 5-10 minutes**

---

## Your Application After Deployment

```
🌐 Public URL:
http://todo-app-env-XXXXX.elasticbeanstalk.com

📊 Dashboard:
https://console.aws.amazon.com/elasticbeanstalk/

📝 Logs:
eb logs

🔍 Status:
eb status

🗑️  Delete (stops costs):
eb terminate todo-app-env
```

---

## Key Commands

```powershell
# Deployment
eb init              # Setup EB (first time)
eb create <env>      # Create environment
eb deploy            # Deploy JAR
eb open              # Open in browser

# Monitoring
eb status            # Check health
eb logs              # View logs
eb ssh               # SSH to instance
eb scale 2           # Scale to 2 instances

# Management
eb list              # Show environments
eb use <env>         # Switch environment
eb config            # View configuration
eb abort             # Cancel deployment
eb terminate <env>   # Delete environment
```

---

## Costs

| Period | Cost | Notes |
|--------|------|-------|
| **First 12 months** | **FREE** ✅ | AWS free tier |
| **After 12 months** | **~$7/month** | t3.micro EC2 only |
| **With ALB** | **~$23/month** | Includes load balancer |

**To keep it free: Use `--single` flag**

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `eb` not found | `pip install awsebcli` |
| AWS credentials error | `aws configure` |
| Build fails | `mvn clean package` |
| App won't start | `eb logs` |
| Can't access URL | Wait 5 min, then `eb open` |
| High AWS bill | `eb terminate environment` |

---

## File Structure

```
todo-app-rest/
├── .ebextensions/java.config     ✅ EB config
├── .ebignore                     ✅ Deploy exclusions
├── src/main/
│   ├── java/TodoApplication.java
│   └── resources/application.properties
├── pom.xml
├── deploy.ps1                    ✅ PowerShell script
└── deploy.bat                    ✅ Batch script

target/
└── fullstack-todo-app-1.0.0.jar  ✅ Built & ready
```

---

## What Gets Deployed

```
fullstack-todo-app-1.0.0.jar (19.05 MB) contains:
├── Spring Boot Application
├── Java Runtime
├── REST API
├── Static Frontend (HTML/CSS/JS)
└── All Dependencies
```

Single JAR deployment - everything included!

---

## Architecture (Simple)

```
You → AWS EC2 → Spring Boot → API & Frontend
     ↑
  Auto-scaling
  Load balancing
  Health monitoring
  CloudWatch logs
```

---

## After Deployment

✅ Your app is live and publicly accessible  
✅ AWS monitors it 24/7  
✅ Auto-scales if traffic increases  
✅ Logs available in CloudWatch  
✅ Deploy updates anytime with `eb deploy`  

---

## Important Reminders

⚠️ **AWS Credentials**
- Keep secret, never commit to git
- Rotate regularly
- Delete unused access keys

⚠️ **Costs**
- Free tier expires after 12 months
- Remember to delete if done: `eb terminate`
- Monitor with AWS Budgets

⚠️ **Security**
- App is public by default
- Can restrict access via Security Groups
- Enable HTTPS if handling sensitive data

---

## Quick Start (Copy These Commands)

```powershell
# 1. First time only
aws configure
cd C:\Users\Jose\fullstack-todo-app\todo-app-rest

# 2. Deploy
mvn clean package
eb init
eb create todo-app-env --instance-type t3.micro --single
eb deploy

# 3. Open
eb open
```

**Done! Your app is live! 🎉**

---

## Need More Help?

📖 **Read In Order:**
1. `QUICK_START.md` - Fast reference
2. `AWS_DEPLOYMENT_GUIDE.md` - Detailed steps
3. `DEPLOYMENT_CHECKLIST.md` - Track progress
4. `ARCHITECTURE_DIAGRAM.md` - Understand it all

🔗 **Resources:**
- AWS Docs: https://docs.aws.amazon.com/elasticbeanstalk/
- Spring Boot: https://spring.io/guides/gs/deploying-spring-boot-app-with-aws/
- AWS Console: https://console.aws.amazon.com/

---

## Status

✅ Application built successfully (19.05 MB JAR)  
✅ Configuration complete  
✅ Deployment scripts ready  
✅ Documentation complete  

**You're ready to deploy!** 🚀

---

*Last Updated: 2026-11-11*  
*Application: fullstack-todo-app v1.0.0*  
*Deployment: AWS Elastic Beanstalk*
