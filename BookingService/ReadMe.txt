1. Get all bookings
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ allBookings { id userId hotelId roomId checkInDate checkOutDate status totalPrice } }"}'

2. Get booking by ID
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"query($id: ID!) { bookingById(id: $id) { id userId hotelId roomId checkInDate checkOutDate status totalPrice } }","variables":{"id":"B123"}}'


👉 Replace B123 with a real booking ID.

3. Get bookings by user ID
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"query($userId: ID!) { bookingsByUser(userId: $userId) { id userId hotelId roomId checkInDate checkOutDate status totalPrice } }","variables":{"userId":"U123"}}'


👉 Replace U123 with an actual user ID.

✍️ Mutations
4. Create a booking
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation { createBooking(userId: \"U123\", hotelId: \"H456\", roomId: \"R101\", checkInDate: \"2025-10-10\", checkOutDate: \"2025-10-15\", totalPrice: 550.75) { id userId hotelId roomId checkInDate checkOutDate status totalPrice } }"}'


👉 Replace U123, H456, R101, dates, and price as needed.

5. Cancel a booking
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation($id: ID!) { cancelBooking(id: $id) { id userId hotelId status } }","variables":{"id":"B123"}}'


👉 Replace B123 with the booking ID to cancel.