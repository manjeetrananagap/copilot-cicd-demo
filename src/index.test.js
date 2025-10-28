const request = require('supertest');
const app = require('./index');
let server;

beforeAll(() => {
    server = app.listen(0); // Use port 0 for random available port
});

afterAll(done => {
    server.close(done);
});

describe('Express API Endpoints', () => {
    describe('POST /api/user/age', () => {
        test('should return correct age for valid user', async () => {
            const response = await request(app)
                .post('/api/user/age')
                .send({
                    user: {
                        birthDate: new Date('2000-01-01'),
                    },
                });
            expect(response.statusCode).toBe(200);
            expect(response.body).toHaveProperty('age', 25); // assuming current year is 2025
        });

        test('should handle invalid user data', async () => {
            const response = await request(app)
                .post('/api/user/age')
                .send({
                    user: {
                        birthDate: 'invalid-date',
                    },
                });
            expect(response.statusCode).toBe(400);
            expect(response.body).toHaveProperty('error');
        });

        test('should handle missing user data', async () => {
            const response = await request(app).post('/api/user/age').send({});
            expect(response.statusCode).toBe(400);
            expect(response.body).toHaveProperty('error');
        });
    });

    describe('POST /api/user/retirement', () => {
        test('should return correct years until retirement for valid user', async () => {
            const response = await request(app)
                .post('/api/user/retirement')
                .send({
                    user: {
                        birthDate: new Date('2000-01-01'),
                    },
                });
            expect(response.statusCode).toBe(200);
            expect(response.body).toHaveProperty('yearsUntilRetirement', 40); // 65 - 25 = 40
        });

        test('should handle invalid user data', async () => {
            const response = await request(app)
                .post('/api/user/retirement')
                .send({
                    user: {
                        birthDate: 'invalid-date',
                    },
                });
            expect(response.statusCode).toBe(400);
            expect(response.body).toHaveProperty('error');
        });

        test('should handle missing user data', async () => {
            const response = await request(app)
                .post('/api/user/retirement')
                .send({});
            expect(response.statusCode).toBe(400);
            expect(response.body).toHaveProperty('error');
        });
    });
});
