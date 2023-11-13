import React from 'react';
import {Form as ReactRouterForm, useLoaderData} from 'react-router-dom';
import {useTranslation} from 'react-i18next';
import Form from 'react-bootstrap/Form';
import FloatingLabel from 'react-bootstrap/FloatingLabel';
import Button from 'react-bootstrap/Button';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';


const TOTPSetup = () => {
  const loaderData = useLoaderData();
  const {t} = useTranslation();

  const totpSetup  = loaderData.totpSetup;
  const qrUrl = loaderData.qrUrl;

  return (
    <>
        <Row>
            <Col>
                <h2>{t('totp.enabling.title')}</h2>
                <h2>{t('totp.shared.key.title')}</h2>
            </Col>
        </Row>
        <Row>
            <Col>
                <img src={qrUrl} alt='QR Code'/>
            </Col>
        </Row>
        <Row>
            <Col>
                <h2>{t('totp.recovery.tokens.title')}</h2>
            </Col>
        </Row>
        <Row>
            <Col>
                <ul>
                    {totpSetup.scratchCodesPlain.map((code, idx) => <li key={idx}>{code}</li>)}
                </ul>
            </Col>
        </Row>
        <Row>
            <Col>
                <ReactRouterForm method='post'>
                    <FloatingLabel label={t('totp.verification.code')}>
                        <Form.Control id='verificationCode' name='verificationCode' type='number' placeholder='000000' />
                    </FloatingLabel>
                    <Form.Control id='sharedKey' name='sharedKey' type='hidden' value={totpSetup.sharedKey}/>
                    {totpSetup.scratchCodes.map((scratchCode, idx) =>
                        <React.Fragment key={idx}>
                            <Form.Control
                                id={'scratchCodes['+idx+'].hasher'}
                                name={'scratchCodes['+idx+'].hasher'}
                                type='hidden'
                                value={scratchCode.hasher}/>
                            <Form.Control
                                id={'scratchCodes['+idx+'].password'}
                                name={'scratchCodes['+idx+'].password'}
                                type='hidden'
                                value={scratchCode.password}/>
                            <Form.Control
                                id={'scratchCodes['+idx+'].salt'}
                                name={'scratchCodes['+idx+'].salt'}
                                type='hidden'
                                value={scratchCode.salt ? scratchCode.salt : ''}/>
                        </React.Fragment>)
                    }
                    <Button type='submit'>{t('totp.verify')}</Button>
                </ReactRouterForm>
            </Col>
        </Row>
    </>
  )
};

export default TOTPSetup;
