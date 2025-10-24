# Setup Instructions

## Prerequisites
- Node.js 16.x or higher
- Visual Studio Code
- Git

## Installation Steps

1. **Extract the ZIP file** to your desired location

2. **Open in VS Code**
   ```bash
   cd copilot-cicd-demo
   code .
   ```

3. **Install Recommended Extensions**
   - VS Code will prompt you to install recommended extensions
   - Click "Install All"
   - Required: GitHub Copilot, GitHub Copilot Chat, ESLint, Prettier, Jest

4. **Install Dependencies**
   ```bash
   npm install
   ```

5. **Verify Setup**
   ```bash
   npm test
   ```
   You should see 4 failing tests (this is expected!)

## For GitHub Integration

1. **Create a new GitHub repository**
   ```bash
   git init
   git add .
   git commit -m "Initial commit with bugs"
   ```

2. **Push to GitHub**
   ```bash
   gh repo create copilot-cicd-demo --public --source=. --push
   ```
   Or create manually on GitHub and push

3. **Enable GitHub Actions**
   - Go to your repo → Actions tab
   - Enable workflows

## Ready to Demo!

Now you can:
1. Show the failing tests
2. Use Copilot to fix bugs
3. Watch tests pass
4. See CI/CD pipeline succeed

For detailed demo instructions, see README.md
