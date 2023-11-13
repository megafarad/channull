import React, {useState} from 'react';
import FloatingLabel from 'react-bootstrap/FloatingLabel';
import Form from 'react-bootstrap/Form';
import InputGroup from 'react-bootstrap/InputGroup';
import Button from 'react-bootstrap/Button';
import {MdVisibility, MdVisibilityOff} from 'react-icons/md';


const PasswordField = ({label, id, name}) => {

    const [visible, setVisible] = useState(false);

    const handleClickShowPassword = () => setVisible((show) => !show);

    return (
        <InputGroup className='mt-3'>
            <FloatingLabel label={label}>
                <Form.Control
                    id={id}
                    name={name}
                    type={visible ? 'text' : 'password'}
                    placeholder='password'
                />
            </FloatingLabel>
            <InputGroup.Text>
                <Button onClick={handleClickShowPassword} variant='link'>
                    {visible ? <MdVisibilityOff/> : <MdVisibility/>}
                </Button>
            </InputGroup.Text>
        </InputGroup>
    );
};

export default PasswordField;
