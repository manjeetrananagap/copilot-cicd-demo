const {
    getUserAge,
    validateUser,
    calculateYearsUntilRetirement,
    formatUserInfo
} = require('./userService');

describe('User Service', () => {
    describe('getUserAge', () => {
        test('should calculate correct age for valid user', () => {
            const user = { birthDate: new Date('2000-01-01') };
            expect(getUserAge(user)).toBe(25); // assuming current year is 2025
        });

        test('should handle null user', () => {
            expect(getUserAge(null)).toBeNull();
        });

        test('should handle user without birthDate', () => {
            expect(getUserAge({})).toBeNull();
        });
    });

    describe('validateUser', () => {
        test('should return true for valid user', () => {
            const user = { birthDate: new Date('2000-01-01') };
            expect(validateUser(user)).toBe(true);
        });

        test('should return false for null user', () => {
            expect(validateUser(null)).toBe(false);
        });

        test('should validate birthDate is a Date object', () => {
            const invalidUser = { birthDate: 'invalid-date' };
            // This test will fail initially due to bug
            expect(validateUser(invalidUser)).toBe(false);
        });
    });

    describe('calculateYearsUntilRetirement', () => {
        test('should calculate years until retirement correctly', () => {
            const user = { birthDate: new Date('2000-01-01') };
            expect(calculateYearsUntilRetirement(user)).toBe(40); // 65 - 25 = 40
        });

        test('should handle users past retirement age', () => {
            const user = { birthDate: new Date('1950-01-01') };
            const years = calculateYearsUntilRetirement(user);
            expect(years).toBeLessThan(0); // Should be negative
        });
    });

    describe('formatUserInfo', () => {
        test('should format user info correctly', () => {
            const user = { 
                name: 'John Doe',
                birthDate: new Date('2000-01-01')
            };
            expect(formatUserInfo(user)).toBe('John Doe is 25 years old');
        });

        test('should handle user without name', () => {
            const user = { birthDate: new Date('2000-01-01') };
            // This test will fail initially due to missing null check
            expect(() => formatUserInfo(user)).not.toThrow();
        });
    });
});
