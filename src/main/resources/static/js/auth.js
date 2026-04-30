const auth = {
    setSession(data) {
        localStorage.setItem('lexguard_token', data.token);
        const userData = {
            nombre: data.nombreCompleto,
            email: data.email,
            rol: data.rol
        };
        if (data.id) {
            userData.id = data.id;
        }
        if (data.especialidad) {
            userData.especialidad = data.especialidad;
        }
        localStorage.setItem('lexguard_user', JSON.stringify(userData));
    },

    clearSession() {
        localStorage.removeItem('lexguard_token');
        localStorage.removeItem('lexguard_user');
    },

    isAuthenticated() {
        return !!localStorage.getItem('lexguard_token');
    },

    getUser() {
        const user = localStorage.getItem('lexguard_user');
        return user ? JSON.parse(user) : null;
    },

    protectRoute() {
        if (!this.isAuthenticated()) {
            window.location.href = 'login.html';
        }
    },
    
    redirectIfAuthenticated() {
        if (this.isAuthenticated()) {
            window.location.href = 'dashboard.html';
        }
    }
};

window.logout = function() {
    auth.clearSession();
    window.location.href = 'index.html';
};
