import React from 'react';
import Header from './Header';
import { useTranslation } from 'react-i18next';
import {Link, Outlet, useLoaderData} from 'react-router-dom';
import ApiResponse from './ApiResponse';
import Button from 'react-bootstrap/Button';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';

const Home = () => {

  const { t } = useTranslation();
  const getUserResponse = useLoaderData();

  const user = getUserResponse.user;
  const totpInfo = getUserResponse.totpInfo;

  return (
    <>
      <Header/>
      <Container>
          <ApiResponse/>
          <Row className='mt-3'>
              <Col>
                  { t('welcome.signed.in') }
              </Col>
              <Col>
                  <img src={user.avatarURL} alt={user.fullName + ' picture'}/>
              </Col>
          </Row>
          <Row>
              <Col>
                  <strong>{t('first.name')}</strong>:&nbsp;
                  {user.firstName ? user.firstName : 'None'}
              </Col>
          </Row>
          <Row>
              <Col>
                  <strong>{t('last.name')}</strong>:&nbsp;
                  {user.lastName ? user.lastName : 'None'}
              </Col>
          </Row>
          <Row>
              <Col>
                  <strong>{t('full.name')}</strong>:&nbsp;
                  {user.fullName ? user.fullName : 'None'}
              </Col>
          </Row>
          <Row>
              <Col>
                  <strong>{t('email')}</strong>:&nbsp;
                  {user.email ? user.email : 'None'}
              </Col>
          </Row>
          {totpInfo ? <Row className='mt-3'>
              <Col>
                  <h2>{t('totp.enabled.title')}</h2>
                  <Link to='/disableTOTP'>
                      <Button>{t('totp.disable')}</Button>
                  </Link>
              </Col>
          </Row> : <Row className='mt-3'>
              <Col>
                  <Link to='totpSetup'>
                      <Button>{t('totp.enable')}</Button>
                  </Link>
              </Col>
          </Row>}
          <Outlet/>
      </Container>
    </>
  );
};

export default Home;
