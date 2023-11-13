import React from 'react';
import {useTranslation} from 'react-i18next';
import {Form as ReactRouterForm, Link} from 'react-router-dom';
import {useSelector} from 'react-redux';
import SignInError from './SignInError';
import Header from './Header';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import FloatingLabel from 'react-bootstrap/FloatingLabel';
import Button from 'react-bootstrap/Button';

const TOTP = () => {
  const {t} = useTranslation();

  const totpChallenge = useSelector((state) => state.user.totpChallenge);

  return (
    <>
      <Header/>
      <Container>
          <SignInError/>
          <Row className='mt-3'>
              <Col>
                  {t('sign.in.totp')}
              </Col>
          </Row>
          <ReactRouterForm method='post'>
              <Row className='mt-3'>
                  <Col>
                      <FloatingLabel label={t('totp.verification.code')}>
                          <Form.Control
                              id='verificationCode'
                              name='verificationCode'
                              type='number'
                              placeholder='000000'
                          />
                      </FloatingLabel>
                      <Form.Control
                          id='userID'
                          name='userID'
                          type='hidden'
                          value={totpChallenge.userID}
                      />
                      <Form.Control
                          id='sharedKey'
                          name='sharedKey'
                          type='hidden'
                          value={totpChallenge.sharedKey}
                      />
                      <Form.Control
                          id='rememberMe'
                          name='rememberMe'
                          type='hidden'
                          value={totpChallenge.rememberMe}
                      />
                  </Col>
              </Row>
              <Row className='mt-3'>
                  <Col>
                      {t('totp.open.the.app.for.2fa')}
                  </Col>
              </Row>
              <Row className='mt-3'>
                  <Col>
                      <Button type='submit'>{t('totp.verify')}</Button>
                  </Col>
              </Row>
          </ReactRouterForm>
          <Row className='mt-3'>
              <Col>
                  {t('totp.dont.have.your.phone')}
              </Col>
          </Row>
          <Row className='mt-3'>
              <Col>
                  <Link to='/totpRecovery'>
                      <Button>{t('totp.use.recovery.code')}</Button>
                  </Link>
              </Col>
          </Row>
      </Container>
    </>
  );
}

export default TOTP;
