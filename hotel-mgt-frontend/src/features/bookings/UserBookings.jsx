// UserBookings.jsx
import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { loadBookingsByUser } from "./bookingSlice";
import { Calendar, Building2, DollarSign, Ban } from "lucide-react";

const formatDate = (dateStr) => {
    if (!dateStr) return "-";
    return new Date(dateStr).toLocaleDateString("en-US", {
        weekday: "short",
        month: "short",
        day: "numeric",
        year: "numeric",
    });
};

const formatPrice = (price) => {
    if (price == null) return "-";
    return `$${price.toFixed(2)} USD`;
};

const UserBookings = ({ userId }) => {
    const dispatch = useDispatch();
    const bookings = useSelector((state) => state.bookings.userBookings);

    useEffect(() => {
        if (userId) dispatch(loadBookingsByUser(userId));
    }, [userId, dispatch]);

    return (
        <div className="p-6">
            <h2 className="text-2xl font-bold mb-6">
                My Bookings{" "}
                <span className="text-gray-500 text-base">(User {userId})</span>
            </h2>

            {bookings.length === 0 ? (
                <div className="flex flex-col items-center justify-center text-gray-500 bg-gray-50 rounded-xl p-10">
                    <Ban className="w-10 h-10 mb-3 text-gray-400" />
                    <p className="text-lg font-medium">No bookings found</p>
                    <p className="text-sm text-gray-400">
                        You haven’t booked any hotels yet. Start exploring!
                    </p>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {bookings.map((b) => (
                        <div
                            key={b.id}
                            className="bg-white shadow-md rounded-2xl p-5 hover:shadow-xl transition"
                        >
                            {/* Hotel Header */}
                            <div className="flex items-center gap-2 mb-2">
                                <Building2 className="w-5 h-5 text-blue-600" />
                                <h3 className="text-lg font-bold">
                                    {b.hotel?.name || "Unknown Hotel"}
                                </h3>
                            </div>
                            <p className="text-sm text-gray-500">{b.hotel?.city || "-"}</p>

                            {/* Dates & Price */}
                            <div className="mt-4 space-y-2 text-sm">
                                <p className="flex items-center gap-2">
                                    <Calendar className="w-4 h-4 text-gray-500" />
                                    <span>
                                        <span className="font-medium">Check-in:</span>{" "}
                                        {formatDate(b.checkInDate)}
                                    </span>
                                </p>
                                <p className="flex items-center gap-2">
                                    <Calendar className="w-4 h-4 text-gray-500" />
                                    <span>
                                        <span className="font-medium">Check-out:</span>{" "}
                                        {formatDate(b.checkOutDate)}
                                    </span>
                                </p>
                                <p className="flex items-center gap-2 font-semibold text-green-600">
                                    <DollarSign className="w-4 h-4" />
                                    {formatPrice(b.totalPrice)}
                                </p>
                            </div>

                            {/* Status Badge */}
                            <div className="mt-4">
                                <span
                                    className={`px-3 py-1 text-xs font-medium rounded-full ${b.status === "CONFIRMED"
                                        ? "bg-green-100 text-green-700"
                                        : b.status === "CANCELLED"
                                            ? "bg-red-100 text-red-700"
                                            : "bg-yellow-100 text-yellow-700"
                                        }`}
                                >
                                    {b.status || "PENDING"}
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