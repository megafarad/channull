import React from 'react';
import Header from './Header';
import {useTranslation} from 'react-i18next';
import {Form as ReactRouterForm} from 'react-router-dom';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';
import PasswordFieldWithStrength from './PasswordFieldWithStrength';
import Container from "react-bootstrap/Container";

const ResetPassword = () => {
  const { t} = useTranslation();

  return (
    <>
        <Header/>
        <ReactRouterForm method='post'>
            <Container>
                <Row className='mt-3'>
                    <Col>
                        {t('reset.password')}
                    </Col>
                </Row>
                <Row className='mt-3'>
                    <Col>
                        <PasswordFieldWithStrength id='password' name='password' label={t('password')} />
                    </Col>
                </Row>
                <Row className='mt-3'>
                    <Col>
                        <Button type='submit'>{t('reset')}</Button>
                    </Col>
                </Row>
            </Container>
        </ReactRouterForm>
    </>
  );
};

export default ResetPassword;
