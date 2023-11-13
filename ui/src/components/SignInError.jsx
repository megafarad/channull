import React from 'react';
import {useSelector} from 'react-redux';
import {useTranslation} from 'react-i18next';
import {useDispatch} from 'react-redux';
import {clearSignInResponse} from '../redux/userSlice';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Alert from 'react-bootstrap/Alert';

const SignInError = () => {
  const signInResponse = useSelector((state) => state.user.signInResponse);
  const dispatch = useDispatch();
  const {t} = useTranslation();

  const onClose = () => {
    dispatch(clearSignInResponse());
  }

  if (signInResponse && signInResponse.error) {
    return (
      <Row>
        <Col>
          <Alert variant='danger' onClose={onClose} dismissible>
            <Alert.Heading>
              {t('error')}
            </Alert.Heading>
            {signInResponse.error}
          </Alert>
        </Col>
      </Row>
    )
  }

  return null;
}

export default SignInError;
