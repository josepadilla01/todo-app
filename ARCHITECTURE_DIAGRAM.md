```
╔══════════════════════════════════════════════════════════════════════════════╗
║                    AWS DEPLOYMENT ARCHITECTURE DIAGRAM                       ║
╚══════════════════════════════════════════════════════════════════════════════╝


YOUR LOCAL MACHINE (Windows)
════════════════════════════════════════════════════════════════════════════════

    C:\Users\Jose\fullstack-todo-app
    │
    ├── src/
    │   └── main/
    │       ├── java/
    │       │   └── TodoApplication.java (Spring Boot app)
    │       └── resources/
    │           └── application.properties
    │
    ├── public/                    (Frontend files)
    │   ├── index.html
    │   ├── app.js
    │   └── styles.css
    │
    └── target/
        └── fullstack-todo-app-1.0.0.jar (19.05 MB) ✅ BUILT!


DEPLOYMENT FLOW
════════════════════════════════════════════════════════════════════════════════

    1. Install Tools              2. Configure Credentials      3. Deploy
    ┌──────────────────┐          ┌─────────────────────┐       ┌─────────┐
    │ AWS CLI          │          │ aws configure       │       │eb deploy│
    │ EB CLI           │  ───────>│ Access Key ID       │ ───> │         │
    │ Maven (mvn)      │          │ Secret Access Key   │      │  Uploads│
    │ Java JDK         │          │ Region: us-east-1   │      │   JAR   │
    └──────────────────┘          └─────────────────────┘      │    to   │
                                                                │   AWS   │
                                                                └─────────┘


AWS ELASTIC BEANSTALK ENVIRONMENT
════════════════════════════════════════════════════════════════════════════════

    ┌─────────────────────────────────────────────────────────────────┐
    │                    AWS Elastic Beanstalk                         │
    │  todo-app-env.elasticbeanstalk.com                              │
    │                                                                  │
    │  ┌──────────────────────────────────────────────────────────┐  │
    │  │          Application Load Balancer                       │  │
    │  │  (Distributes traffic across instances - optional)       │  │
    │  │  Port 80 (HTTP) / 443 (HTTPS)                           │  │
    │  └──────────────┬───────────────────────────────────────────┘  │
    │                 │                                               │
    │  ┌──────────────▼────────────┐  ┌────────────────────────┐    │
    │  │   EC2 Instance (t3.micro) │  │ EC2 Instance (scaled)  │    │
    │  │                           │  │                        │    │
    │  │  ┌─────────────────────┐  │  │ ┌──────────────────┐   │    │
    │  │  │  Spring Boot App    │  │  │ │ Spring Boot App  │   │    │
    │  │  │  • Java Runtime     │  │  │ │ • Java Runtime   │   │    │
    │  │  │  • REST API         │  │  │ │ • REST API       │   │    │
    │  │  │  • Static Frontend  │  │  │ │ • Static Files   │   │    │
    │  │  │  • Running on :5000 │  │  │ │ • Running        │   │    │
    │  │  └─────────────────────┘  │  │ └──────────────────┘   │    │
    │  │                           │  │                        │    │
    │  │  CloudWatch Logs          │  │ CloudWatch Logs        │    │
    │  │  • Application logs       │  │ • Application logs     │    │
    │  │  • System logs            │  │ • System logs          │    │
    │  │  • Spring Boot output     │  │ • Spring Boot output   │    │
    │  └───────────────────────────┘  └────────────────────────┘    │
    │                 │                           │                 │
    │  ┌──────────────▼───────────────────────────▼──────────────┐  │
    │  │        Auto Scaling Group (optional)                    │  │
    │  │        • Min: 1, Max: 6 instances                       │  │
    │  │        • Scales based on CPU/Memory                     │  │
    │  └───────────────────────────────────────────────────────────┘  │
    │                                                                  │
    │  ┌──────────────────────────────────────────────────────────┐  │
    │  │        CloudWatch Monitoring & Alarms                   │  │
    │  │  • CPU Usage          • Network I/O                      │  │
    │  │  • Memory             • Health Status                    │  │
    │  │  • Disk Space         • Application Logs                │  │
    │  └──────────────────────────────────────────────────────────┘  │
    │                                                                  │
    └──────────────────────────────────────────────────────────────────┘


USER ACCESS
════════════════════════════════════════════════════════════════════════════════

    🌐 Browser
       │
       ├─ https://todo-app-env.elasticbeanstalk.com
       │
       ▼
    Application Load Balancer (if enabled)
       │
       ├─ Routes to EC2 Instance 1 (50%)
       ├─ Routes to EC2 Instance 2 (50%)
       └─ Auto-scales if needed
       
       ▼
    Spring Boot Application
       │
       ├─ Serves static files
       │  ├─ index.html
       │  ├─ app.js
       │  └─ styles.css
       │
       └─ REST API endpoints
          ├─ GET /api/todos
          ├─ POST /api/todos
          └─ etc.


CONFIGURATION FILES CREATED
════════════════════════════════════════════════════════════════════════════════

    ✅ .ebextensions/java.config
       └─ Elastic Beanstalk configuration
          • Instance type: t3.micro
          • Java version: Corretto 25
          • CloudWatch logs enabled

    ✅ .ebignore
       └─ Files to exclude from deployment
          • node_modules/, .git/, .vscode/

    ✅ src/main/resources/application.properties
       └─ Spring Boot configuration
          • server.port=5000 (with fallback to PORT env var)
          • static resources configuration
          • compression enabled

    ✅ src/main/resources/application-production.properties
       └─ Production-specific settings
          • Warning log level
          • No error stacktraces


DEPLOYMENT SCRIPTS CREATED
════════════════════════════════════════════════════════════════════════════════

    ✅ deploy.ps1
       └─ PowerShell script (RECOMMENDED)
          • .\deploy.ps1 -Action create    (first time)
          • .\deploy.ps1 -Action deploy    (updates)
          • .\deploy.ps1 -Action status
          • .\deploy.ps1 -Action logs
          • .\deploy.ps1 -Action open

    ✅ deploy.bat
       └─ Batch script for cmd.exe
          • deploy.bat (runs all steps)


DEPLOYMENT TIMELINE
════════════════════════════════════════════════════════════════════════════════

    Step 1: Initialize EB (first time)
    ├─ ✅ Complete:  eb init
    └─ Time: ~30 seconds

    Step 2: Create Environment (first time)
    ├─ Create EC2 instance
    ├─ Configure load balancer (optional)
    ├─ Set up monitoring
    └─ Time: 3-5 minutes

    Step 3: Deploy Application
    ├─ Upload JAR to AWS
    ├─ Extract JAR on instance
    ├─ Start Spring Boot
    ├─ Health checks pass
    ├─ Update load balancer
    └─ Time: 2-3 minutes

    Total First Deployment: 5-10 minutes ⏱️
    
    Subsequent Deployments (just eb deploy): 2-3 minutes


NETWORK FLOW - USER REQUEST
════════════════════════════════════════════════════════════════════════════════

    1. User Action
    ┌──────────────────────────────────┐
    │ Browser opens:                   │
    │ todo-app-env.elasticbeanstalk.   │
    │ com/index.html                   │
    └─────────────────┬────────────────┘
                      │
                      ▼
    2. DNS Resolution
    ┌──────────────────────────────────┐
    │ Route 53 or AWS DNS              │
    │ Resolves to ELB IP               │
    └─────────────────┬────────────────┘
                      │
                      ▼
    3. Load Balancer (if enabled)
    ┌──────────────────────────────────┐
    │ Chooses healthy EC2 instance     │
    │ Forwards HTTP request to port 80 │
    └─────────────────┬────────────────┘
                      │
                      ▼
    4. Spring Boot Application
    ┌──────────────────────────────────┐
    │ Receives request on port 5000     │
    │ Processes and responds           │
    │ Returns HTML/JSON/static files   │
    └─────────────────┬────────────────┘
                      │
                      ▼
    5. Response
    ┌──────────────────────────────────┐
    │ User receives:                   │
    │ • HTML page                      │
    │ • CSS styling                    │
    │ • JavaScript app                 │
    │ • API responses                  │
    └──────────────────────────────────┘


COMMAND REFERENCE
════════════════════════════════════════════════════════════════════════════════

    PowerShell (Recommended)          Batch Script              Manual Commands
    ─────────────────────────────     ──────────────────        ────────────────
    cd C:\...\todo-app-rest          cd C:\...\todo-app-rest    cd C:\...\todo-app-rest

    .\deploy.ps1 -Action create      deploy.bat                 mvn clean package
                                                                eb init
    .\deploy.ps1 -Action deploy                                 eb create ...
                                                                eb deploy
    .\deploy.ps1 -Action status      deploy.bat                 eb status

    .\deploy.ps1 -Action logs                                   eb logs

    .\deploy.ps1 -Action open                                   eb open

    .\deploy.ps1 -Action terminate                              eb terminate


AFTER DEPLOYMENT - ACCESS YOUR APP
════════════════════════════════════════════════════════════════════════════════

    Your app is now live at:
    ┌──────────────────────────────────────────────────┐
    │ http://todo-app-env-XXXXX.elasticbeanstalk.com   │
    │                                                  │
    │ Where XXXXX is auto-generated by AWS             │
    │                                                  │
    │ Example:                                         │
    │ http://todo-app-env-001.elasticbeanstalk.com    │
    └──────────────────────────────────────────────────┘

    Access:
    • Frontend: http://todo-app-env-XXXXX.elasticbeanstalk.com/
    • API: http://todo-app-env-XXXXX.elasticbeanstalk.com/api/todos

    AWS Console:
    • Elastic Beanstalk: console.aws.amazon.com/elasticbeanstalk
    • EC2 Instances: console.aws.amazon.com/ec2/instances
    • CloudWatch Logs: console.aws.amazon.com/logs


MONITORING & MANAGEMENT
════════════════════════════════════════════════════════════════════════════════

    eb status              Check environment status
    eb logs                View application logs
    eb ssh                 SSH into EC2 instance
    eb scale 2             Scale to 2 instances
    eb deploy              Deploy new version
    eb terminate           Delete environment (STOP COSTS!)
    eb config              View/edit environment config
    eb setenv KEY=VALUE    Set environment variables
    eb abort               Cancel deployment


COST CALCULATION
════════════════════════════════════════════════════════════════════════════════

    Configuration Option              Monthly Cost          Notes
    ─────────────────────────────     ───────────────────   ──────────────
    --single + t3.micro               $0 (12 months FREE)   Free tier
                                      $7 (after)            Recommended

    Load Balancer + 2x t3.micro       $0 (eligible)         Free tier
                                      $23 (after)           Production

    Terminating Environment            $0                    Stops all charges


TROUBLESHOOTING GUIDE
════════════════════════════════════════════════════════════════════════════════

    Issue                          Solution
    ─────────────────────────────  ──────────────────────────────
    EB CLI not found               pip install awsebcli
    AWS CLI not found              Download from aws.amazon.com/cli
    AWS credentials error          aws configure
    Build fails                    mvn clean package -X
    App won't start                eb logs
    Can't access URL               Wait 5 min, then eb open
    High AWS bill                  eb terminate environment
    Health check failing           eb logs, check Spring Boot


════════════════════════════════════════════════════════════════════════════════
                             READY TO DEPLOY! 🚀
════════════════════════════════════════════════════════════════════════════════
```
