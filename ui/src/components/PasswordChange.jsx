import React from 'react';
import Header from './Header';
import PasswordField from './PasswordField';
import {useTranslation} from 'react-i18next';
import {Form as ReactRouterForm} from 'react-router-dom';
import PasswordFieldWithStrength from './PasswordFieldWithStrength';
import ApiResponse from './ApiResponse';
import Button from 'react-bootstrap/Button';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';

const PasswordChange = () => {
  const {t} = useTranslation();

  return (
    <>
        <Header/>
        <Container>
            <ApiResponse/>
            <Row className='mt-3'>
                <Col>
                    {t('change.password')}
                </Col>
            </Row>
            <ReactRouterForm method='post'>
                <Row>
                    <Col>
                        <PasswordField
                            label={t('current.password')}
                            id='current-password'
                            name='current-password'
                        />
                    </Col>
                </Row>
                <PasswordFieldWithStrength id='new-password' name='new-password' label={t('new.password')}/>
                <Row className='mt-3'>
                    <Col>
                        <Button type='submit'>{t('change')}</Button>
                    </Col>
                </Row>
            </ReactRouterForm>
        </Container>
    </>
  );
}

export default PasswordChange;
