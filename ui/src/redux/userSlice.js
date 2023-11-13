import {createAsyncThunk, createSlice} from '@reduxjs/toolkit';
import getCsrfTokenCookie from '../util/getCsrfTokenCookie';

export const signIn = createAsyncThunk(
  'user/signIn',
  async (signInForm) => {
    const res = await fetch('/api/signIn', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Csrf-Token' : getCsrfTokenCookie()
      },
      body: JSON.stringify(signInForm),
    });
    return await res.json();
  }
);

export const signOut = createAsyncThunk(
  'user/signOut',
  async () => {
    const res = await fetch('/api/signOut', {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        'Csrf-Token' : getCsrfTokenCookie()
      }
    });
    return await res.json();
  }
);

export const totpSubmit = createAsyncThunk(
  'user/totpSubmit',
  async (totpForm) => {
    const res = await fetch('/api/totpSubmit', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Csrf-Token' : getCsrfTokenCookie()
      },
      body: JSON.stringify(totpForm)
    });
    return await res.json();
  }
);

export const totpRecoverySubmit = createAsyncThunk(
  'user/totpRecoverySubmit',
  async (totpRecoveryForm) => {
    const res = await fetch('/api/totpRecoverySubmit', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Csrf-Token' : getCsrfTokenCookie()
      },
      body: JSON.stringify(totpRecoveryForm)
    });
    return await res.json();
  }
);

export const refreshUser = createAsyncThunk(
  'user/refresh',
  async () => {
    const res = await fetch('/api/user', {
      credentials: 'include'
    });
    return await res.json();
  }
);

export const userSlice = createSlice({
    name: 'user',
    initialState: {
      signInResponse: null,
      totpChallenge: null,
      totpInfo: null,
      user: null,
    },
    reducers: {
      clearSignInResponse: (state) => {
        state.signInResponse = null;
      }
    },
    extraReducers: (builder) => {
      builder.addCase(signIn.fulfilled, (state, action) => {
        state.signInResponse = action.payload;
        if (action.payload.user) {
          state.user = action.payload.user;
        }
        if (action.payload.totpChallenge) {
          state.totpChallenge = action.payload.totpChallenge;
        }
      });
      builder.addCase(signIn.rejected, (state, action) => {
        state.signInResponse = action.error.message;
      });
      builder.addCase(signOut.fulfilled, (state) => {
        state.signInResponse = null;
        state.user = null;
        state.totpChallenge = null;
      });
      builder.addCase(totpSubmit.fulfilled, (state, action) => {
        state.signInResponse = action.payload;
        if (action.payload.user) {
          state.user = action.payload.user;
        }
      });
      builder.addCase(refreshUser.fulfilled, (state, action) => {
        if (action.payload.user) {
          state.user = action.payload.user;
        }
        if (action.payload.totpInfo) {
          state.totpInfo = action.payload.totpInfo;
        }
      });
      builder.addCase(totpRecoverySubmit.fulfilled, (state, action) => {
        state.signInResponse = action.payload;
        if (action.payload.user) {
          state.user = action.payload.user
        }
      })
    }
  }
);

export const {clearSignInResponse} = userSlice.actions
export default userSlice.reducer;
