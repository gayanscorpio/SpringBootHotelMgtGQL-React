//# Component: list all bookings
import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { loadAllBookings } from "./bookingSlice";

const BookingList = () => {
    const dispatch = useDispatch();
    const bookings = useSelector((state) => state.bookings.list);

    useEffect(() => {
        dispatch(loadAllBookings());
    }, [dispatch]);

    return (
        <div>
            <h2>All Bookings</h2>
            <ul>
                {bookings.map((b) => (
                    <li key={b.id}>
                        #{b.id} | User: {b.user?.name} | Hotel: {b.hotel?.name} | Status: {b.status} | ${b.totalPrice}
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default BookingList;
