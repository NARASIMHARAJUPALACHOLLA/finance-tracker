import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import transactionReducer from './slices/transactionSlice';
import budgetReducer from './slices/budgetSlice';
import dashboardReducer from './slices/dashboardSlice';
import insightsReducer from './slices/insightsSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    transactions: transactionReducer,
    budget: budgetReducer,
    dashboard: dashboardReducer,
    insights: insightsReducer
  }
});
