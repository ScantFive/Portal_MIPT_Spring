const API_BASE = import.meta.env.PROD ? '' : 'http://localhost:8082';

// Глобальный обработчик ошибок
async function fetchJson(url, options = {}) {
  const res = await fetch(`${API_BASE}${url}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers
    }
  });

  if (!res.ok) {
    let errorBody = '';
    try {
      errorBody = await res.text();
    } catch {}
    
    const error = new Error(`HTTP ${res.status}: ${res.statusText}`);
    error.status = res.status;
    error.body = errorBody;
    throw error;
  }

  if (res.status === 204) return null;
  return res.json();
}

export const api = {
  // ===== AUTH =====
  auth: {
    login: (email, password) => 
      fetchJson('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
      }),
    // Если у вас нет отдельного эндпоинта /auth/logout, можно добавить позже
    logout: () => {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    }
  },

  // ===== USERS =====
  users: {
    getAll: () => fetchJson('/api/users'),
    getById: (id) => fetchJson(`/api/users/${id}`),
    getByEmail: (email) => 
      fetchJson(`/api/users/by-email?email=${encodeURIComponent(email)}`),
    
    // Регистрация: ваш бэкенд ждёт { login, email, password }
    register: (data) => 
      fetchJson('/api/users', {
        method: 'POST',
        body: JSON.stringify({
          login: data.login,
          email: data.email?.toLowerCase()?.trim(),
          password: data.password
        })
      }),
    
    update: (id, data) => 
      fetchJson(`/api/users/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data)
      }),
    
    delete: (id) => 
      fetchJson(`/api/users/${id}`, { method: 'DELETE' })
  },

  // ===== WALLETS =====
  wallets: {
    getByUserId: (userId) => fetchJson(`/api/wallets/${userId}`),
    // Добавьте другие методы по мере необходимости
  }
};

// Утилита для проверки авторизации
export const auth = {
  getToken: () => localStorage.getItem('token'),
  getUser: () => {
    try {
      return JSON.parse(localStorage.getItem('user'));
    } catch {
      return null;
    }
  },
  isAuthenticated: () => !!auth.getToken(),
  
  // Сохранение данных после успешного логина
  setAuth: (token, user) => {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
  }
};
