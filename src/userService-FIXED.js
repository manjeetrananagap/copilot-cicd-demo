/**
 * Calculate user's age based on birth date
 * Fixed: Correct formula
 */
function getUserAge(user) {
    if (!user || !user.birthDate) {
        return null;
    }
    // Fixed: Correct calculation
    return new Date().getFullYear() - user.birthDate.getFullYear();
}

/**
 * Validate user object
 * Fixed: Complete validation including Date type check
 */
function validateUser(user) {
    if (!user) {
        return false;
    }
    // Fixed: Check if birthDate exists and is a valid Date
    return user.hasOwnProperty('birthDate') && user.birthDate instanceof Date;
}

/**
 * Calculate years until retirement (assuming retirement age is 65)
 * Fixed: Correct calculation
 */
function calculateYearsUntilRetirement(user) {
    const age = getUserAge(user);
    if (age === null) {
        return null;
    }
    // Fixed: Correct order
    return 65 - age;
}

/**
 * Format user info
 * Fixed: Added null checks
 */
function formatUserInfo(user) {
    if (!user || !user.name) {
        return 'Unknown user';
    }
    const age = getUserAge(user);
    // Fixed: Handle null age
    if (age === null) {
        return `${user.name} has invalid birth date`;
    }
    return `${user.name} is ${age} years old`;
}

module.exports = {
    getUserAge,
    validateUser,
    calculateYearsUntilRetirement,
    formatUserInfo
};
