import React from 'react';
import Header from './Header';
import {useTranslation} from 'react-i18next';
import {Form as RectRouterForm, Link} from 'react-router-dom';
import Form from 'react-bootstrap/Form';
import FloatingLabel from 'react-bootstrap/FloatingLabel';
import Button from 'react-bootstrap/Button';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import GoogleButton from 'react-google-button';
import FacebookIcon from '../images/providers/facebook.png';
import GithubIcon from '../images/providers/github.png';
import ApiResponse from './ApiResponse';
import SignInError from './SignInError';
import PasswordField from './PasswordField';


const SignIn = () => {
  const { t} = useTranslation();

  return (
    <>
      <Header/>
      <Container>
          <ApiResponse/>
          <SignInError/>
          <Row className='mt-3'>
              <Col>
                  {t('sign.in.credentials')}
              </Col>
          </Row>
          <RectRouterForm method='post'>
              <Row className='mt-3'>
                  <Col>
                    <FloatingLabel label={t('email')}>
                        <Form.Control
                            id='email'
                            name='email'
                            type='email'
                            placeholder='email@example.com'
                        />
                    </FloatingLabel>
                  </Col>
              </Row>
              <Row className='mt-3'>
                  <Col>
                      <PasswordField id='password' name='password' label={t('password')}/>
                  </Col>
              </Row>
              <Row className='mt-3'>
                  <Col>
                      <Form.Check name='rememberMe' label={t('remember.me')} value={true} />
                  </Col>
              </Row>
              <Row className='mt-3'>
                  <Col>
                      <Button type='submit'>{t('sign.in')}</Button>
                  </Col>
              </Row>
              <Row className='mt-3'>
                  <Col>
                      {t('not.a.member')} <Link to='/signUp'>{t('sign.up.now')}</Link> | <Link to='/password/forgot'>{t('forgot.your.password')}</Link>
                  </Col>
              </Row>
              <Row className='mt-3'>
                  <Col>
                      {t('or.use.social')}
                  </Col>
              </Row>
              <Row className='mt-3'>
                  <Col md={3}>
                      <GoogleButton
                        onClick={() => window.location.href='/authenticate/google'}
                      />
                  </Col>
                  <Col md={3}>
                      <div className='facebook-sign-in-button' onClick={() => window.location.href='/authenticate/facebook'}>
                          <div className='facebook-sign-in-button-icon'>
                              <img src={FacebookIcon} alt='Facebook icon' height='48px' width='48px'/>
                          </div>
                          <span>Sign in with Facebook</span>
                      </div>
                  </Col>
                  <Col md={3}>
                      <div className='github-sign-in-button' onClick={() => window.location.href='/authenticate/github'}>
                          <div className='github-sign-in-button-icon'>
                              <img src={GithubIcon} alt='Github icon' height='48px' width='48px'/>
                          </div>
                          <span>Sign in with Github</span>
                      </div>
                  </Col>
              </Row>
          </RectRouterForm>
      </Container>
    </>
  )
}

export default SignIn;
