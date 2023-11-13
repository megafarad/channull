import React from 'react';
import Header from './Header';
import SignInError from './SignInError';
import {useTranslation} from 'react-i18next';
import {Form as ReactRouterForm} from 'react-router-dom';
import {useSelector} from 'react-redux';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Form from 'react-bootstrap/Form';
import FloatingLabel from 'react-bootstrap/FloatingLabel';
import Button from 'react-bootstrap/Button';

const TOTPRecovery = () => {
  const {t} = useTranslation();

  const totpChallenge = useSelector((state) => state.user.totpChallenge);

  return (
    <>
        <Header/>
        <Container>
            <SignInError/>
            <Row className='mt-3'>
                <Col>
                    {t('sign.in.totp.recovery')}
                </Col>
            </Row>
            <ReactRouterForm method='post'>
                <Row className='mt-3'>
                    <Col>
                        <FloatingLabel label={t('totp.recovery.code')}>
                            <Form.Control
                                id='recoveryCode'
                                name='recoveryCode'
                                type='number'
                                placeholder='00000000'
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
                        <Button type='submit'>{t('totp.verify')}</Button>
                    </Col>
                </Row>
            </ReactRouterForm>
        </Container>
    </>
  );
}

export default TOTPRecovery;
