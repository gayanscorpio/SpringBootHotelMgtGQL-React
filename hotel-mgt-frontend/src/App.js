import React, { useState } from "react";
import BookingList from "./features/bookings/BookingList";
import BookingDetail from "./features/bookings/BookingDetail";
import UserBookings from "./features/bookings/UserBookings";

function App() {
  const [bookingId, setBookingId] = useState("");
  const [userId, setUserId] = useState("");
  const [searchBooking, setSearchBooking] = useState("");
  const [searchUser, setSearchUser] = useState("");

  const handleBookingSearch = () => setBookingId(searchBooking);
  const handleUserSearch = () => setUserId(searchUser);

  return (
    <div className="p-6">
      <h1 className="text-3xl font-bold mb-6">Hotel Booking Management</h1>

      {/* --- Booking Search --- */}
      <div className="mb-4">
        <input
          type="text"
          placeholder="Enter Booking ID"
          value={searchBooking}
          onChange={(e) => setSearchBooking(e.target.value)}
          className="border px-3 py-2 rounded mr-2"
        />
        <button
          onClick={handleBookingSearch}
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          Search Booking
        </button>
      </div>

      {bookingId && <BookingDetail bookingId={bookingId} />}

      <hr className="my-6" />

      {/* --- User Search --- */}
      <div className="mb-4">
        <input
          type="text"
          placeholder="Enter User ID"
          value={searchUser}
          onChange={(e) => setSearchUser(e.target.value)}
          className="border px-3 py-2 rounded mr-2"
        />
        <button
          onClick={handleUserSearch}
          className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
        >
          Search User Bookings
        </button>
      </div>

      {userId && <UserBookings userId={userId} />}

      <hr className="my-6" />

      {/* --- All bookings list --- */}
      <BookingList />
    </div>
  );
}

export default App;
