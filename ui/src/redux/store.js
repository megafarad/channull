import { configureStore } from '@reduxjs/toolkit';
import userReducer from './userSlice';
import apiResponseReducer from './apiResponseSlice';

export default configureStore({
  reducer: {
    user: userReducer,
    apiResponse: apiResponseReducer
  }
});
