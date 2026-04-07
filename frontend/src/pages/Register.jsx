import { useState } from 'react';
import { api } from '../api/client';
import { useNavigate, Link } from 'react-router-dom';
import './Register.css';

export default function Register() {
  const [form, setForm] = useState({ login: '', email: '', password: '', confirmPassword: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError(''); // сброс ошибки при вводе
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // === ВАЛИДАЦИЯ (соответствует бэкенду) ===
    
    // 1. Email домен @phystech.edu
    if (!form.email.trim().endsWith('@phystech.edu')) {
      return setError('Регистрация доступна только для почты @phystech.edu');
    }

    // 2. Логин >= 3 символа
    if (form.login.trim().length < 3) {
      return setError('Логин должен содержать не менее 3 символов');
    }

    // 3. Пароль >= 6 символов
    if (form.password.length < 6) {
      return setError('Пароль должен содержать не менее 6 символов');
    }

    // 4. Подтверждение пароля
    if (form.password !== form.confirmPassword) {
      return setError('Пароли не совпадают');
    }

    setLoading(true);
    try {
      // Запрос к бэкенду
      await api.users.register({
        login: form.login.trim(),
        email: form.email.trim(),
        password: form.password
      });
      
      // Успех
      alert('✅ Регистрация успешна! Теперь войдите в систему.');
      navigate('/login');
      
    } catch (err) {
      // Обработка ошибок от Spring
      if (err.status === 409) {
        setError('Пользователь с таким email уже существует');
      } else if (err.status === 400) {
        // Парсинг сообщения от бэкенда, если оно есть
        const msg = err.body || 'Неверные данные. Проверьте поля формы';
        setError(msg);
      } else {
        setError(err.message || 'Ошибка при регистрации. Попробуйте позже.');
      }
      console.error('Registration error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <h2>Регистрация</h2>
      
      <form onSubmit={handleSubmit} className="auth-form">
        {error && <div className="error">{error}</div>}
        
        <input
          type="text"
          name="login"
          placeholder="Логин"
          value={form.login}
          onChange={handleChange}
          required
          minLength={3}
          disabled={loading}
        />
        <input
          type="email"
          name="email"
          placeholder="Email (@phystech.edu)"
          value={form.email}
          onChange={handleChange}
          required
          pattern=".*@phystech\.edu$"
          disabled={loading}
        />
        <input
          type="password"
          name="password"
          placeholder="Пароль"
          value={form.password}
          onChange={handleChange}
          required
          minLength={6}
          disabled={loading}
        />
        <input
          type="password"
          name="confirmPassword"
          placeholder="Подтвердите пароль"
          value={form.confirmPassword}
          onChange={handleChange}
          required
          disabled={loading}
        />
        
        <button type="submit" disabled={loading}>
          {loading ? 'Создание...' : 'Зарегистрироваться'}
        </button>
      </form>
      
      <p className="auth-link">
        Уже есть аккаунт? <Link to="/login">Войти</Link>
      </p>
    </div>
  );
}
