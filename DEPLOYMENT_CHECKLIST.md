# AWS Elastic Beanstalk Deployment Checklist
# Follow these steps in order to successfully deploy your app

## Pre-Deployment Checklist

- [ ] **AWS Account Created**
  - Go to https://aws.amazon.com/
  - Create a free tier account

- [ ] **IAM Access Keys Created**
  - Log into AWS Console
  - Go to Security Credentials
  - Create Access Key ID and Secret Access Key
  - Save them somewhere safe

- [ ] **AWS CLI Installed**
  - Run: `aws --version`
  - If not installed, download from https://aws.amazon.com/cli/

- [ ] **EB CLI Installed**
  - Run: `pip install awsebcli`
  - Verify: `eb --version`

- [ ] **AWS Credentials Configured**
  - Run: `aws configure`
  - Enter your Access Key ID
  - Enter your Secret Access Key
  - Enter region (e.g., us-east-1)

- [ ] **Verify Credentials Work**
  - Run: `aws sts get-caller-identity`
  - Should show your AWS account info

- [ ] **Java and Maven Installed**
  - Run: `java -version`
  - Run: `mvn --version`

## Deployment Steps

1. [ ] **Open PowerShell/Command Prompt**
   - Navigate to: `C:\Users\Jose\fullstack-todo-app\todo-app-rest`
   - Command: `cd C:\Users\Jose\fullstack-todo-app\todo-app-rest`

2. [ ] **Build the Application**
   - Command: `mvn clean package`
   - Verify success - should see `BUILD SUCCESS`
   - Check for JAR file in `target\` folder

3. [ ] **Initialize Elastic Beanstalk**
   - Command: `eb init`
   - Application name: `todo-app` (or your choice)
   - Platform: Select `Java` → `Corretto 25`
   - CodeCommit: Type `n` (no)

4. [ ] **Create Environment**
   - Command: `eb create todo-app-env --instance-type t3.micro --single`
   - Wait for completion (3-5 minutes)
   - Note: `--single` = no load balancer (free tier eligible)
   - Without `--single` = load balancer included (costs ~$16/month)

5. [ ] **Deploy Application**
   - Command: `eb deploy`
   - Monitor progress in terminal
   - Should complete in 2-3 minutes

6. [ ] **Verify Deployment**
   - Command: `eb status`
   - Should show "Status: Ready"
   - Command: `eb open`
   - App should open in your browser

## Post-Deployment

- [ ] **Test Your Application**
  - Verify frontend loads (HTML/CSS/JS)
  - Test API endpoints
  - Verify static resources serve correctly

- [ ] **Monitor Logs**
  - Command: `eb logs`
  - Check for any errors

- [ ] **Scale Environment (Optional)**
  - Command: `eb scale 2`
  - Creates 2 instances for high availability

- [ ] **Set Production Profile (Optional)**
  - Add to `.elasticbeanstalk\config.yml`:
    ```
    option_settings:
      aws:elasticbeanstalk:container:java:
        JVM_ARGS: "-Dspring.profiles.active=production"
    ```
  - Then run: `eb deploy`

## Cost Management

- **Free Tier Components** (first 12 months):
  - EC2 t3.micro: 750 hours/month (free)
  - Data transfer: Free for first 15GB/month

- **Charged Components**:
  - Load Balancer (if not using --single): ~$16/month
  - Data transfer beyond 15GB: ~$0.12/GB
  - Elastic IP: $3.25/month (if you allocate one)

- **To Keep Costs at $0/month**:
  - Use `--single` flag (no load balancer)
  - Use `t3.micro` instance type
  - Don't allocate Elastic IP
  - Keep data transfer under 15GB/month

- **To Delete Everything (Stop Charges)**:
  - Command: `eb terminate todo-app-env`
  - Confirm: Type the environment name

## Troubleshooting

### If `eb init` fails:
- Ensure AWS credentials are configured: `aws configure`
- Ensure AWS credentials are valid: `aws sts get-caller-identity`

### If `eb create` fails:
- Check AWS console for any blocking policies
- Try different region: `eb init --region us-west-2`

### If `eb deploy` fails:
- Check build success: Look for `target/*.jar`
- View logs: `eb logs`
- SSH to instance: `eb ssh` (then check `/var/log/eb-engine.log`)

### If app is deployed but shows error page:
- View logs: `eb logs`
- Check application.properties is correct
- Ensure frontend files are in `public/` folder

### If you can't access the app:
- Wait 5 minutes for DNS propagation
- Check Elastic Beanstalk console for health status
- Verify Security Group allows HTTP (port 80)

## Useful Commands

```powershell
eb init              # Initialize EB in current directory
eb create <env>     # Create new environment
eb deploy           # Deploy current JAR to EB
eb status           # Check environment status
eb open             # Open app in browser
eb logs             # View application logs
eb ssh              # SSH into EC2 instance
eb scale 2          # Scale to 2 instances
eb terminate <env>  # Delete environment
eb list             # List all environments
eb use <env>        # Switch active environment
eb abort            # Abort current deployment
eb abort --no-cancel-timeout  # Force abort
```

## Quick Start One-Liner (after setup)

```powershell
# From todo-app-rest folder:
mvn clean package; eb init -p "Java 25" "todo-app"; eb create todo-app-env --instance-type t3.micro --single; eb deploy; eb open
```

---

Good luck with your deployment! 🚀

For more help:
- AWS EB Docs: https://docs.aws.amazon.com/elasticbeanstalk/
- Spring Boot on AWS: https://spring.io/guides/gs/deploying-spring-boot-app-with-aws/
