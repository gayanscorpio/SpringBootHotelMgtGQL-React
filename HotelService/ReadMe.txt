1. Fetch all hotels
curl -X POST http://localhost:8082/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ allHotels { id name city address stars } }"}'

2. Fetch hotel by ID
curl -X POST http://localhost:8082/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ hotelById(id: \"H123\") { id name city address stars } }"}'


🔑 Replace "H123" with an actual hotel ID.

3. Fetch multiple hotels by IDs
curl -X POST http://localhost:8082/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ hotelsByIds(ids: [\"H123\", \"H456\"]) { id name city address stars } }"}'

4. Create a new hotel
curl -X POST http://localhost:8082/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query":"mutation { createHotel(hotel: { name: \"Hilton\", city: \"Colombo\", address: \"123 Main St\", stars: 5 }) { id name city address stars } }"
  }'
  
  👉 Notes:

id is optional in HotelInput. If you don’t pass it, the backend will generate a UUID.

If you want to provide a custom ID:

curl -X POST http://localhost:8082/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query":"mutation { createHotel(hotel: { id:\"H999\", name: \"Marriott\", city: \"Kandy\", address: \"456 Lake Road\", stars: 4 }) { id name city address stars } }"
  }'