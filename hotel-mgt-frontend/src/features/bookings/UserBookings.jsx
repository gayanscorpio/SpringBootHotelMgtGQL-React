// UserBookings.jsx
import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { loadBookingsByUser } from "./bookingSlice";
import { Calendar, Building2, DollarSign } from "lucide-react";

const UserBookings = ({ userId }) => {
    const dispatch = useDispatch();
    const bookings = useSelector((state) => state.bookings.userBookings);

    useEffect(() => {
        if (userId) dispatch(loadBookingsByUser(userId));
    }, [userId, dispatch]);

    return (
        <div className="p-6">
            <h2 className="text-2xl font-bold mb-4">
                My Bookings <span className="text-gray-500">(User {userId})</span>
            </h2>

            {bookings.length === 0 ? (
                <div className="text-gray-500">No bookings found.</div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {bookings.map((b) => (
                        <div
                            key={b.id}
                            className="bg-white shadow-lg rounded-2xl p-5 hover:shadow-xl transition"
                        >
                            <div className="flex items-center gap-2 mb-2">
                                <Building2 className="w-5 h-5 text-blue-600" />
                                <h3 className="text-lg font-bold">{b.hotel?.name || "Unknown Hotel"}</h3>
                            </div>
                            <p className="text-sm text-gray-500">{b.hotel?.city}</p>

                            <div className="mt-3 space-y-1 text-sm">
                                <p className="flex items-center gap-2">
                                    <Calendar className="w-4 h-4 text-gray-500" />
                                    Check-in: {b.checkIn}
                                </p>
                                <p className="flex items-center gap-2">
                                    <Calendar className="w-4 h-4 text-gray-500" />
                                    Check-out: {b.checkOut}
                                </p>
                                <p className="flex items-center gap-2 font-semibold text-green-600">
                                    <DollarSign className="w-4 h-4" />
                                    ${b.totalPrice}
                                </p>
                            </div>

                            <div className="mt-3">
                                <span
                                    className={`px-3 py-1 text-xs font-medium rounded-full ${b.status === "CONFIRMED"
                                        ? "bg-green-100 text-green-700"
                                        : "bg-red-100 text-red-700"
                                        }`}
                                >
                                    {b.status}
                                </span>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default UserBookings;