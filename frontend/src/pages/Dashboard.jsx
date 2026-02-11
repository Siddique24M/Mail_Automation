import React, { useEffect, useState } from 'react';
import { getEvents, logout, syncEvents, getUserInfo } from '../services/api';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [syncing, setSyncing] = useState(false);
    const [filter, setFilter] = useState('All');
    const [userInfo, setUserInfo] = useState({ name: 'User', email: '' });
    const navigate = useNavigate();

    useEffect(() => {
        fetchEvents();
        fetchUserInfo();
    }, []);

    const fetchUserInfo = async () => {
        const data = await getUserInfo();
        setUserInfo(data);
    };

    const fetchEvents = async () => {
        const data = await getEvents();
        setEvents(data);
        setLoading(false);
    };

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    const handleSync = async () => {
        setSyncing(true);
        await syncEvents();
        await fetchEvents();
        setSyncing(false);
    };

    const formatDate = (dateString, time = false) => {
        if (!dateString) return 'N/A';
        try {
            const date = new Date(dateString);
            if (isNaN(date.getTime())) return 'N/A';
            const options = { year: 'numeric', month: 'short', day: 'numeric' };
            if (time) {
                options.hour = '2-digit';
                options.minute = '2-digit';
            }
            return date.toLocaleDateString('en-US', options);
        } catch (error) {
            return 'N/A';
        }
    };

    const filteredEvents = events.filter(event => {
        if (filter === 'All') return true;
        if (filter === 'Interview') return event.eventType === 'Interview';
        if (filter === 'Exam') return event.eventType === 'Exam';
        if (filter === 'Other') return event.eventType !== 'Interview' && event.eventType !== 'Exam';
        return true;
    });

    if (loading) {
        return <div className="container" style={{ textAlign: 'center', marginTop: '5rem' }}>Loading...</div>;
    }

    return (
        <div className="container" style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
            <header style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', marginBottom: '3rem', paddingTop: '1rem' }}>
                {/* Row 1: Logo Left, Title Right */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
                    <div style={{
                        width: '80px',
                        height: '80px',
                        borderRadius: '50%',
                        background: 'linear-gradient(135deg, var(--primary-color), var(--accent-color))',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontWeight: 'bold',
                        color: 'white',
                        fontSize: '2.5rem',
                        boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
                    }}>
                        sm
                    </div>
                    <h1 style={{ fontSize: '2.5rem', margin: 0 }}>Dashboard</h1>
                </div>

                {/* Row 2: Filters Centered */}
                <div style={{ display: 'flex', justifyContent: 'center', width: '100%' }}>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                        {['All', 'Interview', 'Exam', 'Other'].map(f => (
                            <button
                                key={f}
                                onClick={() => setFilter(f)}
                                className={filter === f ? 'btn-primary' : 'glass-card'}
                                style={{
                                    padding: '0.5rem 1.5rem',
                                    fontSize: '0.9rem',
                                    cursor: 'pointer',
                                    border: 'none',
                                    borderRadius: '8px',
                                    background: filter === f ? 'var(--primary-color)' : 'rgba(255, 255, 255, 0.05)',
                                    color: 'var(--text-primary)'
                                }}
                            >
                                {f}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Row 3: Controls Evenly Spaced */}
                <div style={{ display: 'flex', justifyContent: 'space-evenly', alignItems: 'center', width: '100%', padding: '0 1rem' }}>
                    <button
                        onClick={handleSync}
                        disabled={syncing}
                        className="btn-secondary"
                        style={{ padding: '0.75rem 2rem', fontSize: '1rem', opacity: syncing ? 0.7 : 1, borderRadius: '12px' }}
                    >
                        {syncing ? 'Syncing...' : 'Sync Emails'}
                    </button>
                    <span className="glass-card" style={{ padding: '0.75rem 2rem', fontSize: '1rem', borderRadius: '12px' }}>
                        Logged in as {userInfo.name}
                    </span>
                    <button
                        onClick={handleLogout}
                        className="btn-primary"
                        style={{ padding: '0.75rem 2rem', fontSize: '1rem', borderRadius: '12px' }}
                    >
                        Switch Account
                    </button>
                </div>
            </header>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1.5rem', flex: 1 }}>
                {filteredEvents.map((event) => {
                    const isUrgent = event.eventType === 'Exam' || event.eventType === 'Interview';
                    const cardStyle = isUrgent ? { borderLeft: '4px solid var(--danger-color)' } : { borderLeft: '4px solid var(--success-color)' };

                    return (
                        <div key={event.id} className="glass-card" style={{ ...cardStyle }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                                <span style={{
                                    fontSize: '0.75rem',
                                    textTransform: 'uppercase',
                                    letterSpacing: '0.05em',
                                    padding: '4px 8px',
                                    borderRadius: '4px',
                                    background: isUrgent ? 'rgba(239, 68, 68, 0.2)' : 'rgba(34, 197, 94, 0.2)',
                                    color: isUrgent ? '#fca5a5' : '#86efac'
                                }}>
                                    {event.eventType}
                                </span>
                                <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                                    {formatDate(event.eventDate)}
                                </span>
                            </div>
                            <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>{event.companyName}</h3>
                            {event.senderEmail && (
                                <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.5rem' }}>
                                    From: {event.senderEmail}
                                </p>
                            )}
                            {event.actionLink && (
                                <a
                                    href={event.actionLink}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    style={{ color: 'var(--accent-color)', textDecoration: 'none', fontSize: '0.9rem', display: 'inline-block', marginTop: '0.5rem' }}
                                >
                                    View Link &rarr;
                                </a>
                            )}
                        </div>
                    );
                })}

                {filteredEvents.length === 0 && (
                    <div className="glass-card" style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '3rem' }}>
                        <p style={{ color: 'var(--text-secondary)' }}>No events found for this filter.</p>
                    </div>
                )}
            </div>

            <footer style={{
                marginTop: '3rem',
                padding: '1.5rem',
                textAlign: 'center',
                borderTop: '1px solid rgba(255,255,255,0.1)',
                color: 'var(--text-secondary)',
                fontSize: '0.9rem'
            }}>
                <p>&copy; {new Date().getFullYear()} Siddique Mujawar. All rights reserved.</p>
                <p style={{ fontSize: '0.8rem', opacity: 0.7, marginTop: '0.5rem' }}>Personal Email Assistant</p>
            </footer>
        </div>
    );
};

export default Dashboard;
