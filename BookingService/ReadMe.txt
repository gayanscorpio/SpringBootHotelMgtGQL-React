1️⃣ allBookings
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "query { allBookings { id userId hotelId roomId checkInDate checkOutDate status totalPrice } }"}'

2️⃣ bookingById
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "query { bookingById(id: \"BOOKING123\") { id userId hotelId roomId status totalPrice } }"}'

3️⃣ bookingsByUser
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "query { bookingsByUser(userId: \"U123\") { id hotelId checkInDate checkOutDate status totalPrice } }"}'

4️⃣ createBooking
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "mutation { createBooking(userId: \"U123\", hotelId: \"H567\", roomId: \"101\", checkInDate: \"2025-10-01\", checkOutDate: \"2025-10-05\", totalPrice: 250.0) { id status totalPrice } }"}'

5️⃣ cancelBooking
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "mutation { cancelBooking(id: \"BOOKING123\") { id status } }"}'
