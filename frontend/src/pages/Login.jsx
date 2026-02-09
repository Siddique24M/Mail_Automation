import React from 'react';
import { loginWithGoogle } from '../services/api';
import '../index.css';

const Login = () => {
    return (
        <div className="container" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
            <div className="glass-card" style={{ textAlign: 'center', maxWidth: '400px', width: '100%' }}>
                <h1 style={{ marginBottom: '1rem', fontSize: '2.5rem', background: 'linear-gradient(to right, #60a5fa, #a78bfa)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
                    Personal Assistant
                </h1>
                <p style={{ color: 'var(--text-secondary)', marginBottom: '2rem' }}>
                    Automate your email tracking for interviews and exams.
                </p>
                <button onClick={loginWithGoogle} className="btn-primary">
                    Sign in with Google
                </button>
            </div>
        </div>
    );
};

export default Login;
