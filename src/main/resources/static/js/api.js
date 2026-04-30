const API_URL = 'http://localhost:8080/api';

async function fetchApi(endpoint, options = {}) {
    const token = localStorage.getItem('lexguard_token');
    
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        ...options,
        headers
    };

    try {
        const response = await fetch(`${API_URL}${endpoint}`, config);
        
        if (response.status === 401) {
            localStorage.removeItem('lexguard_token');
            localStorage.removeItem('lexguard_user');
            window.location.href = 'login.html';
            return { error: 'Sesión expirada o no autorizada.', status: response.status, isError: true };
        }

        if (response.status === 403) {
            return { error: 'Acceso denegado. No tienes permisos para esta acción.', status: 403, isError: true };
        }

        if (response.ok) {
            if (response.status === 204) return null;
            return await response.json();
        }

        const errorData = await response.json();
        return { error: errorData, status: response.status, isError: true };

    } catch (error) {
        console.error('Network execution error:', error);
        return { error: { error: 'Error de conexión con el servidor.' }, isError: true };
    }
}
