import {createAsyncThunk, createSlice} from '@reduxjs/toolkit';
import getCsrfTokenCookie from '../util/getCsrfTokenCookie';

export const signUp = createAsyncThunk(
  'api/signUp',
  async (signUpForm) => {
    const res = await fetch('/api/signUp', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Csrf-Token' : getCsrfTokenCookie()
      },
      body: JSON.stringify(signUpForm),
    });
    return await res.json();
  }
);

export const activateAccount = createAsyncThunk(
  'api/activateAccount',
  async (token) => {
    const res = await fetch(`/api/account/activate/${token}`)
    return await res.json();
  }
);

export const submitTOTPSetup = createAsyncThunk(
  'api/submitTOTPSetup',
  async (form) => {
    const res = await fetch('/api/totpSetup', {
      method: 'POST',
      headers: {
        'Csrf-Token' : getCsrfTokenCookie()
      },
      credentials: 'include',
      body: form
    });
    return await res.json();
  }
);

export const disableTOTP = createAsyncThunk(
  'api/disableTOTP',
  async () => {
    const res = await fetch('/api/disableTotp', {
      method: 'POST',
      headers: {
        'Csrf-Token' : getCsrfTokenCookie()
      },
      credentials: 'include',
    });
    return await res.json();
  }
);

export const changePassword = createAsyncThunk(
  'api/changePassword',
  async (changePasswordForm) => {
    const res = await fetch('/api/password/change', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Csrf-Token' : getCsrfTokenCookie()
      },
      credentials: 'include',
      body: JSON.stringify(changePasswordForm)
    });
    return await res.json();
  }
);

export const forgotPassword = createAsyncThunk(
  'api/forgotPassword',
  async (forgotPasswordForm) => {
    const res = await fetch('/api/password/forgot', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Csrf-Token' : getCsrfTokenCookie()
      },
      body: JSON.stringify(forgotPasswordForm)
    });
    return await res.json();
  }
);

export const verifyResetToken = createAsyncThunk(
  'api/verifyResetToken',
  async (token) => {
    const res = await fetch(`/api/password/reset/${token}`);
    return await res.json();
  }
);

export const resetPassword = createAsyncThunk(
  'api/resetPassword',
  async ({token, password}) => {
    const res = await fetch(`/api/password/reset/${token}` , {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Csrf-Token' : getCsrfTokenCookie()
      },
      body: JSON.stringify({password: password})
    });
    return await res.json();
  }
);

export const apiResponseSlice = createSlice({
  name: 'apiResponse',
  initialState: {
    response: null
  },
  reducers: {
    clearApiResponse: (state) => {
      state.response = null;
    }
  },
  extraReducers: (builder) => {
    builder.addCase(signUp.fulfilled, (state, action) => {
      state.response = action.payload;
    });
    builder.addCase(activateAccount.fulfilled, (state, action) => {
      state.response = action.payload;
    });
    builder.addCase(submitTOTPSetup.fulfilled, (state, action) => {
      state.response = action.payload;
    });
    builder.addCase(disableTOTP.fulfilled, (state, action) => {
      state.response = action.payload;
    });
    builder.addCase(changePassword.fulfilled, (state, action) => {
      state.response = action.payload;
    });
    builder.addCase(forgotPassword.fulfilled, (state, action) => {
      state.response = action.payload;
    });
    builder.addCase(verifyResetToken.fulfilled, (state, action) => {
      state.response = action.payload;
    })
    builder.addCase(verifyResetToken.rejected, (state) => {
      state.response = {
        'error' : 'Invalid reset token!'
      };
    });
    builder.addCase(resetPassword.fulfilled, (state, action) => {
      state.response = action.payload;
    });
  }
});

export const {clearApiResponse} = apiResponseSlice.actions;
export default apiResponseSlice.reducer;
