import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

export const fetchBudget = createAsyncThunk('budget/fetch', async (_, { rejectWithValue }) => {
  try {
    const res = await api.get('/budget');
    return res.data.data;
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Failed to load budget');
  }
});

export const saveBudget = createAsyncThunk('budget/save', async (payload, { rejectWithValue }) => {
  try {
    const res = await api.post('/budget', payload);
    return res.data.data;
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Failed to save budget');
  }
});

const budgetSlice = createSlice({
  name: 'budget',
  initialState: {
    monthlyBudget: 0,
    categoryBudgets: [],
    status: 'idle',
    error: null
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchBudget.pending, (state) => { state.status = 'loading'; })
      .addCase(fetchBudget.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.monthlyBudget = action.payload.monthlyBudget;
        state.categoryBudgets = action.payload.categoryBudgets || [];
      })
      .addCase(fetchBudget.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      })
      .addCase(saveBudget.fulfilled, (state, action) => {
        state.monthlyBudget = action.payload.monthlyBudget;
        state.categoryBudgets = action.payload.categoryBudgets || [];
      });
  }
});

export default budgetSlice.reducer;
