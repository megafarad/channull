import React, { useState, useEffect, useDeferredValue } from 'react';
import { zxcvbnOptions, zxcvbnAsync } from '@zxcvbn-ts/core';
import { matcherPwnedFactory } from '@zxcvbn-ts/matcher-pwned';
import {useTranslation} from 'react-i18next';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import InputGroup from 'react-bootstrap/InputGroup';
import FloatingLabel from 'react-bootstrap/FloatingLabel';
import Form from 'react-bootstrap/Form';
import Button from 'react-bootstrap/Button';
import {MdVisibility, MdVisibilityOff} from 'react-icons/md';

const matcherPwned = matcherPwnedFactory(fetch, zxcvbnOptions);
zxcvbnOptions.addMatcher('pwned', matcherPwned);

const loadOptions = async () => {
  const zxcvbnCommonPackage = await import(
      /* webpackChunkName: "zxcvbnCommonPackage" */ '@zxcvbn-ts/language-common'
      );
  const zxcvbnEnPackage = await import(
      /* webpackChunkName: "zxcvbnEnPackage" */ '@zxcvbn-ts/language-en'
      );
  return ({
    dictionary: {
      ...zxcvbnCommonPackage.dictionary,
      ...zxcvbnEnPackage.dictionary,
    },
    graphs: zxcvbnCommonPackage.adjacencyGraphs,
    useLevenshteinDistance: true,
    translations: zxcvbnEnPackage.translations,
  })
}
loadOptions().then((options) => {
  zxcvbnOptions.setOptions(options);
});

const usePasswordStrength = (password) => {
  const [result, setResult] = useState(null);
  const deferredPassword = useDeferredValue(password);

  useEffect(() => {
    zxcvbnAsync(deferredPassword).then((response) => setResult(response));
  }, [deferredPassword]);

  return result;
}

const PasswordFieldWithStrength = ({id, name, label}) => {
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const {t} = useTranslation();
  const strengthResult = usePasswordStrength(password);
  const strengthRatings= {
    0: t('worst.password.strength'),
    1: t('bad.password.strength'),
    2: t('weak.password.strength'),
    3: t('good.password.strength'),
    4: t('strong.password.strength')
  };

  const strengthScore = strengthResult ? strengthResult.score : null;

  const strengthRating = strengthScore || strengthScore === 0 ? strengthRatings[strengthScore] : null;

  const handleClickShowPassword = () => setShowPassword((show) => !show);


  return (
      <>
        <Row className='mt-3'>
          <Col>
            {t('strong.password.info')}
          </Col>
        </Row>
        <Row>
          <Col>
            <InputGroup className='mt-3'>
              <FloatingLabel label={label}>
                <Form.Control
                  id={id}
                  name={name}
                  type={showPassword ? 'text' : 'password'}
                  placeholder='password'
                  onChange={(e) => {
                    setPassword(e.target.value);
                  }}
                />
              </FloatingLabel>
              <InputGroup.Text>
                <Button onClick={handleClickShowPassword} variant='link'>
                  {showPassword ? <MdVisibilityOff/> : <MdVisibility/>}
                </Button>
              </InputGroup.Text>
            </InputGroup>
          </Col>
        </Row>
        <Row className='mt-3'>
          <Col sm={2}>
            {t('password.strength')}
          </Col>
          <Col>
            <strong>{strengthRating}: </strong> <meter className='meter' value={strengthScore} max={4}/>
          </Col>
        </Row>
      </>
  );
}

export default PasswordFieldWithStrength;

