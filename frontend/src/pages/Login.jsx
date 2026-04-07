import { useState } from 'react';
import { api, auth } from '../api/client';
import { useNavigate, Link } from 'react-router-dom';
import './Login.css';

export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!form.email.trim() || !form.password) {
      return setError('Заполните все поля');
    }

    if (!form.email.trim().endsWith('@phystech.edu')) {
      return setError('Вход доступен только для @phystech.edu');
    }

    setLoading(true);
    try {
      const response = await api.auth.login(form.email, form.password);
      
      if (response.token) {
        auth.setAuth(response.token, response.user || response);
        navigate('/profile', { replace: true });
      } else if (response.id || response.email) {
        localStorage.setItem('user', JSON.stringify(response));
        navigate('/profile', { replace: true });
      } else {
        setError('Неверный формат ответа от сервера');
      }
      
    } catch (err) {
      console.error('Login error:', err);
      
      if (err.status === 401) {
        setError('Неверный email или пароль');
      } else if (err.status === 404) {
        setError('Пользователь не найден');
      } else if (err.status === 400) {
        setError('Некорректные данные');
      } else if (err.message?.includes('Failed to fetch')) {
        setError('Нет соединения с сервером');
      } else {
        setError('Ошибка при входе. Попробуйте позже.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <h2>Вход</h2>
      <p className="hint">Только для @phystech.edu</p>
      
      <form onSubmit={handleSubmit} className="auth-form">
        {error && <div className="error">{error}</div>}
        
        <input
          type="email"
          name="email"
          placeholder="Email"
          value={form.email}
          onChange={handleChange}
          required
          pattern=".*@phystech\.edu$"
          disabled={loading}
          autoComplete="email"
        />
        
        <input
          type="password"
          name="password"
          placeholder="Пароль"
          value={form.password}
          onChange={handleChange}
          required
          disabled={loading}
          autoComplete="current-password"
        />
        
        <button type="submit" disabled={loading}>
          {loading ? 'Вход...' : 'Войти'}
        </button>
      </form>
      
      <div className="auth-links">
        <p>
          Нет аккаунта? <Link to="/register">Зарегистрироваться</Link>
        </p>
        <p>
          <Link to="/forgot-password">Забыли пароль?</Link>
        </p>
      </div>
    </div>
  );
}
