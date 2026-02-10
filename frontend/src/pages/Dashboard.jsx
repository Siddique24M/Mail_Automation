import React, { useEffect, useState } from 'react';
import { getEvents, logout, syncEvents } from '../services/api';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [syncing, setSyncing] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        fetchEvents();
    }, []);

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
        await fetchEvents(); // Refresh list after sync
        setSyncing(false);
    };

    const formatDate = (dateString, time = false) => {
        if (!dateString) return 'N/A';
        const options = { year: 'numeric', month: 'short', day: 'numeric' };
        if (time) {
            options.hour = '2-digit';
            options.minute = '2-digit';
        }
        return new Date(dateString).toLocaleDateString('en-US', options);
    };

    if (loading) {
        return <div className="container" style={{ textAlign: 'center', marginTop: '5rem' }}>Loading...</div>;
    }

    return (
        <div className="container">
            <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '3rem' }}>
                <h1 style={{ fontSize: '2rem' }}>Dashboard</h1>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    <button
                        onClick={handleSync}
                        disabled={syncing}
                        className="btn-secondary"
                        style={{ padding: '0.5rem 1rem', fontSize: '0.9rem', marginRight: '0.5rem', opacity: syncing ? 0.7 : 1 }}
                    >
                        {syncing ? 'Syncing...' : 'Sync Emails'}
                    </button>
                    <span className="glass-card" style={{ padding: '0.5rem 1rem', fontSize: '0.9rem' }}>
                        Logged in as User
                    </span>
                    <button
                        onClick={handleLogout}
                        className="btn-primary"
                        style={{ padding: '0.5rem 1rem', fontSize: '0.9rem' }}
                    >
                        Switch Account
                    </button>
                </div>
            </header>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.5rem' }}>
                {events.map((event) => {
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

                {events.length === 0 && (
                    <div className="glass-card" style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '3rem' }}>
                        <p style={{ color: 'var(--text-secondary)' }}>No upcoming events found in your emails.</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Dashboard;
