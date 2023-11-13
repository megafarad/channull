import React from 'react';
import {clearApiResponse} from '../redux/apiResponseSlice';
import {useSelector, useDispatch} from 'react-redux';
import {useTranslation} from 'react-i18next';
import Alert from 'react-bootstrap/Alert';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';

const ApiResponse = () => {
  const apiResponse = useSelector((state) => state.apiResponse.response);
  const dispatch = useDispatch();
  const { t } = useTranslation();

  const onClose = () => {
    dispatch(clearApiResponse());
  }

  if (apiResponse) {
    if (apiResponse.info) {
      return (
        <Row>
            <Col>
              <Alert variant='info' onClose={onClose} dismissible>
                <Alert.Heading>
                  {t('info')}
                </Alert.Heading>
                {apiResponse.info}
              </Alert>
            </Col>
        </Row>
      );
    }
    if (apiResponse.success) {
      return (
          <Row>
            <Col>
              <Alert variant='success' onClose={onClose} dismissible>
                <Alert.Heading>
                  {t('success')}
                </Alert.Heading>
                {apiResponse.success}
              </Alert>
            </Col>
          </Row>
      );
    }
    if (apiResponse.error) {
      return (
          <Row>
            <Col>
              <Alert variant='danger' onClose={onClose} dismissible>
                <Alert.Heading>
                  {t('error')}
                </Alert.Heading>
                {apiResponse.error}
              </Alert>
            </Col>
          </Row>
      );
    }

  }

  return null;


}

export default ApiResponse;
