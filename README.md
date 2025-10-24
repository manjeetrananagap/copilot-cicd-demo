# GitHub Copilot CI/CD Demo - VS Code Edition

A comprehensive Node.js demonstration project optimized for **Visual Studio Code** with GitHub Copilot integration.

## 🎯 Quick Start in VS Code

### 1. Install Recommended Extensions
When you open this project in VS Code, you'll be prompted to install:
- GitHub Copilot
- GitHub Copilot Chat
- ESLint
- Prettier
- Jest Runner
- GitLens

**Just click "Install All"!**

### 2. Install Dependencies
```bash
npm install
```

### 3. Run Tests (They Will Fail!)
```bash
npm test
```
You should see **4 failing tests** - this is expected!

## 🐛 The 4 Intentional Bugs

### Bug 1: Age Calculation (src/userService.js, line 9)
- **Issue**: `user.birthDate.getFullYear() - new Date().getFullYear()`
- **Result**: Returns negative age
- **Fix**: Reverse the subtraction order

### Bug 2: Validation Logic (src/userService.js, line 22)
- **Issue**: Missing type check for birthDate
- **Fix**: Add `&& user.birthDate instanceof Date`

### Bug 3: Retirement Calculation (src/userService.js, line 33)
- **Issue**: `age - 65` instead of `65 - age`
- **Fix**: Correct the subtraction order

### Bug 4: Missing Null Checks (src/userService.js, line 42)
- **Issue**: No validation before accessing user.name
- **Fix**: Add null/undefined checks

## 🎬 Demo Flow

### Phase 1: Show the Problem
1. Open Test Explorer (beaker icon in sidebar)
2. Click "Run Tests" → See 4 failures ❌
3. Show the GitHub Actions pipeline (will fail when pushed)

### Phase 2: Fix with Copilot
1. Open `src/userService.js`
2. Fix each bug using Copilot suggestions:
   - Start editing the buggy line
   - Copilot suggests the fix
   - Press Tab to accept
3. Use Copilot Chat for explanations

### Phase 3: Verify Success
1. Run tests again → All pass ✅
2. Push to GitHub → CI pipeline passes ✅

## 🚀 Available Commands

```bash
npm test              # Run tests with coverage
npm run test:watch    # Run tests in watch mode
npm start             # Start the Express server
npm run dev           # Start with nodemon (auto-reload)
npm run lint          # Check code quality
npm run lint:fix      # Auto-fix linting issues
npm run format        # Format code with Prettier
```

## 🐞 Debugging in VS Code

### Debug the Server:
1. Open `src/index.js`
2. Set a breakpoint
3. Press F5 → Select "Launch Server"

### Debug Tests:
1. Open `src/userService.test.js`
2. Set a breakpoint
3. Press F5 → Select "Debug Current Test"

## 💡 Copilot Tips

### Ask Copilot Chat:
- "Fix the age calculation bug in getUserAge"
- "Add null checks to formatUserInfo"
- "Generate test cases for edge cases"
- "Explain why this calculation is wrong"

### Use Inline Chat (Ctrl+I):
- Highlight buggy code
- Press Ctrl+I
- Type: "/fix this bug"

## 📁 Project Structure

```
copilot-cicd-demo/
├── .vscode/                    # VS Code configuration
├── src/
│   ├── index.js               # Express server
│   ├── userService.js         # 4 intentional bugs
│   ├── userService.test.js    # Test suite
│   └── userService-FIXED.js   # Reference solution
├── .github/workflows/
│   └── ci-cd.yml              # GitHub Actions
└── package.json               # Dependencies
```

## 🎓 What You'll Learn

1. How AI detects logic errors in real-time
2. Using Copilot to fix bugs faster
3. Integrating AI into CI/CD workflows
4. Best practices for AI-assisted development

## 🌟 Features

- ✅ GitHub Copilot pre-configured
- ✅ Auto-format on save
- ✅ Auto-fix ESLint issues
- ✅ Debug configurations
- ✅ Test coverage visualization
- ✅ GitHub Actions CI/CD

---

**Ready to demonstrate AI-powered DevOps!** 🚀
