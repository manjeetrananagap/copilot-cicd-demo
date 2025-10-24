/**
 * Calculate user's age based on birth date
 * Bug: Formula is reversed!
 */
function getUserAge(user) {
    if (!user || !user.birthDate) {
        return null;
    }
    // Bug: This returns a negative age!
    return user.birthDate.getFullYear() - new Date().getFullYear();
}

/**
 * Validate user object
 * Bug: Missing validation for birthDate type
 */
function validateUser(user) {
    if (!user) {
        return false;
    }
    // Bug: Should also check if birthDate is a valid Date object
    return user.hasOwnProperty('birthDate');
}

/**
 * Calculate years until retirement (assuming retirement age is 65)
 * Bug: Wrong calculation order
 */
function calculateYearsUntilRetirement(user) {
    const age = getUserAge(user);
    // Bug: Should be 65 - age, not age - 65
    return age - 65;
}

/**
 * Format user info
 * Bug: Missing null checks
 */
function formatUserInfo(user) {
    // Bug: Will crash if user.name is undefined
    return `${user.name} is ${getUserAge(user)} years old`;
}

module.exports = {
    getUserAge,
    validateUser,
    calculateYearsUntilRetirement,
    formatUserInfo
};
