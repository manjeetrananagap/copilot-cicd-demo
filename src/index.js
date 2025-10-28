const express = require('express');
const { getUserAge, calculateYearsUntilRetirement } = require('./userService');

const app = express();
app.use(express.json());

// Middleware to parse dates from JSON
app.use((req, res, next) => {
    if (req.body && req.body.user && req.body.user.birthDate) {
        const date = new Date(req.body.user.birthDate);
        if (isNaN(date.getTime())) {
            return res.status(400).json({ error: 'Invalid birth date format' });
        }
        req.body.user.birthDate = date;
    }
    next();
});

// Added proper error handling and validation
app.post('/api/user/age', (req, res) => {
    if (!req.body.user) {
        return res.status(400).json({ error: 'Missing user data' });
    }

    const age = getUserAge(req.body.user);
    if (age === null) {
        return res
            .status(400)
            .json({ error: 'Invalid user data or birth date' });
    }

    res.json({ age });
});

// Added proper error handling and validation
app.post('/api/user/retirement', (req, res) => {
    if (!req.body.user) {
        return res.status(400).json({ error: 'Missing user data' });
    }

    const years = calculateYearsUntilRetirement(req.body.user);
    if (years === null) {
        return res
            .status(400)
            .json({ error: 'Invalid user data or birth date' });
    }

    res.json({ yearsUntilRetirement: years });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});

module.exports = app;
