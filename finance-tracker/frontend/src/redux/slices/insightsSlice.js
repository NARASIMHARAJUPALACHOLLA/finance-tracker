import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

export const fetchInsights = createAsyncThunk('insights/fetch', async (_, { rejectWithValue }) => {
  try {
    const res = await api.post('/ai/insights');
    return res.data.data;
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Failed to load insights');
  }
});

export const fetchPrediction = createAsyncThunk('insights/predict', async (_, { rejectWithValue }) => {
  try {
    const res = await api.post('/ai/predict');
    return res.data.data;
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Failed to load prediction');
  }
});

const insightsSlice = createSlice({
  name: 'insights',
  initialState: {
    insights: [],
    recommendations: [],
    provider: null,
    prediction: null,
    status: 'idle',
    predictStatus: 'idle',
    error: null
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchInsights.pending, (state) => { state.status = 'loading'; state.error = null; })
      .addCase(fetchInsights.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.insights = action.payload.insights;
        state.recommendations = action.payload.recommendations;
        state.provider = action.payload.provider;
      })
      .addCase(fetchInsights.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.payload;
      })
      .addCase(fetchPrediction.pending, (state) => { state.predictStatus = 'loading'; })
      .addCase(fetchPrediction.fulfilled, (state, action) => {
        state.predictStatus = 'succeeded';
        state.prediction = action.payload;
      })
      .addCase(fetchPrediction.rejected, (state, action) => {
        state.predictStatus = 'failed';
        state.error = action.payload;
      });
  }
});

export default insightsSlice.reducer;
