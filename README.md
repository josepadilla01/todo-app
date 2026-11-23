# React todo application

This project contains a React/Vite frontend and a Spring Boot backend.
Cognito sign-in uses a server-side OAuth session; the browser does not receive
a Cognito client secret. Aurora stores todos by Cognito user.

## Requirements

- Node.js compatible with the versions in `frontend/package.json` and npm
- Java 21 or newer and Maven
- An accessible PostgreSQL database and a configured Cognito user pool and
  confidential app client

## Configuration

Set these variables in the terminal running the backend, or in your deployment
environment. Never commit passwords or client secrets.

- `DB_URL`: PostgreSQL JDBC URL, including the database name
- `DB_USER`, `DB_PASSWORD`: database credentials
- `COGNITO_CLIENT_ID`, `COGNITO_CLIENT_SECRET`: Cognito app client settings
- `COGNITO_ISSUER_URI`: `https://cognito-idp.<region>.amazonaws.com/<pool-id>`
- `COGNITO_DOMAIN`: HTTPS Cognito managed-login domain without a path
- `COGNITO_LOGOUT_URI`: exact registered allowed sign-out URL

The backend uses Hibernate `update`; existing rows without an owner remain
invisible to users. Use reviewed migrations for production and rotate any
credentials previously exposed in the original project.

## Development

In PowerShell, from this project directory:

```powershell
cd frontend
npm install
npm run dev
```

In a second terminal, set the backend variables and run:

```powershell
cd backend
mvn spring-boot:run
```

Open `http://localhost:3000/`. Vite proxies `/api`, `/oauth2`, `/login`,
and `/logout` to the backend at port 5000. The Cognito app client must allow
`http://localhost:3000/login/oauth2/code/cognito` as a callback and
`http://localhost:3000/` as a sign-out URL; set `COGNITO_LOGOUT_URI` to that
sign-out URL when using Vite. These must match exactly.

## Build and verification

Build the frontend **before** packaging the backend. Maven copies `frontend/dist`
into the JAR so Spring serves the UI and API from the same origin.

```powershell
cd frontend
npm ci
npm run lint
npm test
npm run build
cd ..\backend
mvn test
mvn package
```

The JAR is `backend\target\fullstack-todo-app-1.0.0.jar`. Configure Cognito's
callback as `https://<app-host>/login/oauth2/code/cognito` and sign-out URL as
`https://<app-host>/` for deployment, then use that sign-out URL for
`COGNITO_LOGOUT_URI`. Configure HTTPS, trusted proxy headers, database network
access, and a secure secrets provider. For multiple backend instances, use
shared sessions or session affinity.

`backend/Procfile` supports Elastic Beanstalk's Java platform. Its command
expects the JAR at `target\fullstack-todo-app-1.0.0.jar` relative to the bundle
root, with `Procfile` at that root. No AWS resources are created by this project.
