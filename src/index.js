const express = require('express');
const { getUserAge, validateUser, calculateYearsUntilRetirement } = require('./userService');

const app = express();
app.use(express.json());

// Bug 1: Missing error handling
app.post('/api/user/age', (req, res) => {
    const age = getUserAge(req.body.user);
    res.json({ age });
});

// Bug 2: Incorrect calculation logic
app.post('/api/user/retirement', (req, res) => {
    const years = calculateYearsUntilRetirement(req.body.user);
    res.json({ yearsUntilRetirement: years });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});

module.exports = app;
