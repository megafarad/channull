import React from 'react';
import {createBrowserRouter, redirect, RouterProvider} from 'react-router-dom';
import {useDispatch} from 'react-redux';
import '../App.css';
import Home from './Home';
import SignUp from './SignUp';
import SignIn from './SignIn';
import TOTP from './TOTP';
import TOTPSetup from './TOTPSetup';
import {refreshUser, signIn, signOut, totpRecoverySubmit, totpSubmit} from '../redux/userSlice';
import {
  activateAccount,
  changePassword,
  clearApiResponse,
  disableTOTP, forgotPassword, resetPassword,
  signUp,
  submitTOTPSetup, verifyResetToken
} from '../redux/apiResponseSlice';
import TOTPRecovery from "./TOTPRecovery";
import PasswordChange from "./PasswordChange";
import ForgotPassword from "./ForgotPassword";
import ResetPassword from "./ResetPassword";

const App = () => {
  const dispatch = useDispatch();

  const router = createBrowserRouter([
    {
      element: <Home/>,
      path: "/",
      loader: async () => {
        const actionCreator = await dispatch(refreshUser());
        if (actionCreator.payload.error) {
          return redirect('/signIn');
        }
        return actionCreator.payload;
      },
      children: [
        {
          path: 'totpSetup',
          element: <TOTPSetup/>,
          loader: async () => {
            const res = await fetch('/api/totpSetup', {
              credentials: 'include'
            });
            return res.json();
          },
          action: async ({request}) => {
            const formData = await request.formData();
            const setupResult = await dispatch(submitTOTPSetup(formData));
            if (setupResult.type.endsWith('rejected') || (setupResult.payload && setupResult.payload.error)) {
              return setupResult;
            }
            return redirect('/');
          }
        }
      ]
    },
    {
      path: '/disableTOTP',
      loader: async () => {
        await dispatch(disableTOTP());
        return redirect('/');
      }
    },
    {
      path: '/totpRecovery',
      element: <TOTPRecovery/>,
      action: async ({request}) => {
        const recoveryFormData = await request.formData();
        const recoveryFormJson = Object.fromEntries(recoveryFormData);
        const recoveryFormResult = await dispatch(totpRecoverySubmit(recoveryFormJson));
        if (recoveryFormResult.type.endsWith('rejected')|| (recoveryFormResult.payload && recoveryFormResult.payload.error)) {
          return recoveryFormResult;
        }
        return redirect('/');
      }
    },
    {
      element: <SignUp/>,
      path: '/signUp',
      action: async ({request}) => {
        const formData = await request.formData();
        const formJson = Object.fromEntries(formData);
        const signupResult = await dispatch(signUp(formJson));
        if (signupResult.type.endsWith('rejected') || (signupResult.payload && signupResult.payload.error)) {
          return signupResult;
        }
        return redirect('/signIn');
      }
    },
    {
      element: <SignIn/>,
      path: '/signIn',
      action: async ({request}) => {
        const formData = await request.formData();
        const formJson = Object.fromEntries(formData);
        const formResult = await dispatch(signIn(formJson));
        await dispatch(clearApiResponse());
        if (formResult.type.endsWith('rejected') || (formResult.payload && formResult.payload.error)) {
          return formResult;
        }
        if (formResult.payload && formResult.payload.totpChallenge) {
          return redirect('/totp');
        }
        return redirect('/');
      }
    },
    {
      element: <TOTP/>,
      path: '/totp',
      action: async ({request}) => {
        const formData = await request.formData();
        const formJson = Object.fromEntries(formData);
        const formResult = await dispatch(totpSubmit(formJson));
        if (formResult.type.endsWith('rejected') || (formResult.payload && formResult.payload.error)) {
          return formResult;
        }
        return redirect('/');
      }
    },
    {
      path: '/signOut',
      loader: async () => {
        await dispatch(signOut());
        return redirect('/');
      }
    },
    {
      path: '/password/forgot',
      element: <ForgotPassword/>,
      action: async ({request}) => {
        const formData = await request.formData();
        const formJson = Object.fromEntries(formData);
        const formResult = await dispatch(forgotPassword(formJson));
        if (formResult.type.endsWith('rejected') || (formResult.payload && formResult.payload.error)) {
          return formResult;
        }
        return redirect('/signIn');
      }
    },
    {
      path: '/password/change',
      element: <PasswordChange/>,
      action: async ({request}) => {
        const formData = await request.formData();
        const formJson = Object.fromEntries(formData);
        const formResult = await dispatch(changePassword(formJson));
        if (formResult.type.endsWith('rejected') || (formResult.payload && formResult.payload.error)) {
          return formResult;
        }
        return redirect('/');
      }
    },
    {
      path: '/password/reset/:token',
      element: <ResetPassword/>,
      loader: async ({params}) => {
        const verifyResult = await dispatch(verifyResetToken(params.token));
        if (verifyResult.type.endsWith('rejected') || (verifyResult.payload && verifyResult.payload.error)) {
          return redirect('/signIn');
        }
        return verifyResult;
      },
      action: async ({params, request}) => {
        const passwordForm = await request.formData();
        const password = passwordForm.get('password');
        const resetResult = await dispatch(resetPassword({password: password, token: params.token}));
        if (resetResult.type.endsWith('rejected') || (resetResult.payload && resetResult.payload.error)) {
          return resetResult;
        }
        return redirect('/signIn');
      }
    },
    {
      path: '/account/activate/:activationKey',
      loader: async ({params}) => {
        await dispatch(activateAccount(params.activationKey));
        return redirect('/signIn');
      }
    }
  ])
  return (<RouterProvider router={router}/>);
}


export default App;
