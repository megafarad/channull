import React from 'react';
import { Link } from 'react-router-dom';
import Container from 'react-bootstrap/Container';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import { useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';

const Header = () => {
  const { t } = useTranslation();
  const user = useSelector((state) => state.user.user);

  return (
    <Navbar expand='lg' bg='dark' data-bs-theme='dark'>
      <Container>
        <Navbar.Brand><Link to='/' className='nav-link'>Silhouette React Seed Template</Link></Navbar.Brand>
        <Navbar.Toggle aria-controls='basic-navbar-nav'/>
        <Navbar.Collapse id='basic-navbar-nav' className='justify-content-end'>
          <Nav className='ml-auto'>
            { user ? <Link to='/' className='nav-link'>{user.fullName}</Link> : null}
            { user ? <Link to='/signOut' className='nav-link'>{t('sign.out')}</Link> : null }
            { !user ?
                <>
                  <Link to='/signIn' className='nav-link'>{t('sign.in')}</Link>
                  <Link to='/signUp' className='nav-link'>{t('sign.up')}</Link>
                </> : null
            }
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default Header;
