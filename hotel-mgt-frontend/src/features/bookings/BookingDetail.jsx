//# Component: view booking by ID
import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { loadBookingById } from "./bookingSlice";

const BookingDetail = ({ bookingId }) => {
    const dispatch = useDispatch();
    const booking = useSelector((state) => state.bookings.selected);

    useEffect(() => {
        if (bookingId) dispatch(loadBookingById(bookingId));
    }, [bookingId, dispatch]);

    if (!booking) return <p>Loading...</p>;

    return (
        <div>
            <h2>Booking #{booking.id}</h2>
            <p>User: {booking.user?.name}</p>
            <p>Hotel: {booking.hotel?.name} ({booking.hotel?.city})</p>
            <p>Status: {booking.status}</p>
            <p>Total Price: ${booking.totalPrice}</p>
        </div>
    );
};

export default BookingDetail;
