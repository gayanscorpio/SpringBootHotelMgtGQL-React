import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { fetchAllBookings, fetchBookingById, fetchBookingsByUser } from "../../api/bookingApi";

export const loadAllBookings = createAsyncThunk("bookings/loadAll", async () => {
    return await fetchAllBookings();
});

export const loadBookingById = createAsyncThunk("bookings/loadById", async (id) => {
    return await fetchBookingById(id);
});

export const loadBookingsByUser = createAsyncThunk("bookings/loadByUser", async (userId) => {
    return await fetchBookingsByUser(userId);
});

const bookingSlice = createSlice({
    name: "bookings",
    initialState: { list: [], selected: null, userBookings: [], status: "idle", error: null },
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(loadAllBookings.fulfilled, (state, action) => {
                state.list = action.payload.allBookings;
            })
            .addCase(loadBookingById.fulfilled, (state, action) => {
                state.selected = action.payload?.bookingById || null;
            })
            .addCase(loadBookingsByUser.fulfilled, (state, action) => {
                state.userBookings = action.payload.bookingsByUser;
            });
    },
});

export default bookingSlice.reducer;
