import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:9090',
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
    window.location.href = 'http://localhost:9090/login/google';
};

export default api;
