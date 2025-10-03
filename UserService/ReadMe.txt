1. Fetch all users
curl -X POST http://localhost:8083/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ allUsers { id name email } }"}'

2. Fetch user by ID
curl -X POST http://localhost:8083/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"query($id: ID!) { userById(id: $id) { id name email } }","variables":{"id":"U123"}}'

3. Fetch multiple users by IDs
curl -X POST http://localhost:8083/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"query($ids:[ID!]!){ usersByIds(ids:$ids) { id name email } }","variables":{"ids":["U123","U456"]}}'

4. Create a new user (updated customeUser input)
curl -X POST http://localhost:8083/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation { createUser(customeUser: { name: \"John Doe\", email: \"john@example.com\" }) { id name email } }"}'