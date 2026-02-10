import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:9090',
});

export const getEvents = async () => {
    try {
        const response = await api.get('/api/events');
        return response.data;
    } catch (error) {
        console.error("Error fetching events", error);
        return [];
    }
};

export const loginWithGoogle = () => {
    const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:9090';
    window.location.href = `${baseUrl}/login/google`;
};

export const logout = async () => {
    try {
        await api.post('/logout');
    } catch (error) {
        console.error("Error logging out", error);
    }
};

export default api;
