1. Get all bookings
GraphQL Query
query {
  allBookings {
    id
    userId
    hotelId
    roomId
    checkInDate
    checkOutDate
    status
    totalPrice
    user {
      id
      name
      email
    }
    hotel {
      id
      name
      city
    }
  }
}

Curl Request

curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ allBookings { id userId hotelId roomId checkInDate checkOutDate status totalPrice user { id name email } hotel { id name city } } }"}'


2. Get booking by ID
GraphQL Query
query {
  bookingById(id: "B001") {
    id
    userId
    hotelId
    roomId
    checkInDate
    checkOutDate
    status
    totalPrice
    user {
      id
      name
      email
    }
    hotel {
      id
      name
      city
    }
  }
}




Curl Request

curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ bookingById(id: \"B001\") { id userId hotelId roomId checkInDate checkOutDate status totalPrice user { id name email } hotel { id name city } } }"}'



3. Get bookings by User ID
GraphQL Query

query {
  bookingsByUser(userId: "U100") {
    id
    userId
    hotelId
    roomId
    checkInDate
    checkOutDate
    status
    totalPrice
    user {
      id
      name
      email
    }
    hotel {
      id
      name
      city
    }
  }
}


Curl Request

curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ bookingsByUser(userId: \"U100\") { id userId hotelId roomId checkInDate checkOutDate status totalPrice user { id name email } hotel { id name city } } }"}'

  
  