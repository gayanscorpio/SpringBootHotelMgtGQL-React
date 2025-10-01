// # GraphQL queries/mutations
const GRAPHQL_URL = "http://localhost:8081/graphql"; // Facade endpoint

export async function gqlRequest(query, variables = {}) {
    const response = await fetch(GRAPHQL_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ query, variables }),
    });
    const data = await response.json();
    return data.data;
}

// Queries
export const fetchAllBookings = () =>
    gqlRequest(`query { allBookings { id userId hotelId status totalPrice user { name email } hotel { name city } } }`);

export const fetchBookingById = (id) =>
    gqlRequest(`query($id: String!) { bookingById(id: $id) { id userId hotelId status totalPrice user { name } hotel { name } } }`, { id });

export const fetchBookingsByUser = (userId) =>
    gqlRequest(`query($userId: String!) { bookingsByUser(userId: $userId) { id hotelId status totalPrice hotel { name city } } }`, { userId });
