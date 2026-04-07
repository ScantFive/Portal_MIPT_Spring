import { useEffect, useState } from 'react';
import { api, session } from '../api/client';
import { useNavigate } from 'react-router-dom';
import './Profile.css';

export default function Profile() {
  const [user, setUser] = useState(null);
  const [balance, setBalance] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [editMode, setEditMode] = useState({ login: false, email: false, password: false });
  const [formData, setFormData] = useState({});
  const navigate = useNavigate();

  // Загрузка данных профиля
  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      // Проверяем сессию и загружаем данные
      const [userData, balanceData] = await Promise.all([
        api.profile.me(),
        // balance загружаем, если в ответе есть userID
        // api.profile.getBalance(userData.userID)
      ]);
      
      setUser(userData);
      // setBalance(balanceData);
    } catch (err) {
      // Если сессия невалидна — редирект на логин
      if (err.status === 401 || err.status === 403) {
        session.clear();
        navigate('/login', { replace: true });
        return;
      }
      setError('Не удалось загрузить профиль');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = async () => {
    try {
      await api.auth.logout();
    } finally {
      session.clear();
      navigate('/login', { replace: true });
    }
  };

  const handleEditSubmit = async (field) => {
    try {
      if (field === 'login') {
        await api.profile.updateLogin(formData.newLogin);
      } else if (field === 'email') {
        await api.profile.updateEmail(formData.currentPassword, formData.newEmail);
      } else if (field === 'password') {
        if (formData.newPassword !== formData.confirmPassword) {
          throw new Error('Пароли не совпадают');
        }
        await api.profile.updatePassword(
          formData.oldPassword, 
          formData.newPassword, 
          formData.confirmPassword
        );
      }
      
      // Обновляем данные после успешного изменения
      await loadProfile();
      setEditMode({ ...editMode, [field]: false });
      setFormData({});
    } catch (err) {
      setError(err.message || 'Ошибка при обновлении');
    }
  };

  if (loading) return <div className="container">⏳ Загрузка профиля...</div>;
  if (error) return <div className="container error">❌ {error}</div>;
  if (!user) return null;

  return (
    <div className="profile-container">
      <header className="profile-header">
        <h1>Профиль</h1>
        <button onClick={handleLogout} className="btn-logout">Выйти</button>
      </header>

      <section className="profile-card">
        <h2>Личная информация</h2>
        
        {/* Логин */}
        <div className="profile-field">
          <label>Логин</label>
          {editMode.login ? (
            <div className="edit-form">
              <input
                type="text"
                value={formData.newLogin || user.login}
                onChange={(e) => setFormData({ ...formData, newLogin: e.target.value })}
                minLength={3}
              />
              <div className="edit-actions">
                <button onClick={() => handleEditSubmit('login')}>Сохранить</button>
                <button onClick={() => setEditMode({ ...editMode, login: false })}>Отмена</button>
              </div>
            </div>
          ) : (
            <div className="field-value">
              {user.login}
              <button onClick={() => setEditMode({ ...editMode, login: true })}>Изменить</button>
            </div>
          )}
        </div>

        {/* Email */}
        <div className="profile-field">
          <label>Email</label>
          {editMode.email ? (
            <div className="edit-form">
              <input
                type="email"
                value={formData.newEmail || user.email}
                onChange={(e) => setFormData({ ...formData, newEmail: e.target.value })}
                required
              />
              <input
                type="password"
                placeholder="Текущий пароль для подтверждения"
                value={formData.currentPassword || ''}
                onChange={(e) => setFormData({ ...formData, currentPassword: e.target.value })}
                required
              />
              <div className="edit-actions">
                <button onClick={() => handleEditSubmit('email')}>Сохранить</button>
                <button onClick={() => setEditMode({ ...editMode, email: false })}>Отмена</button>
              </div>
            </div>
          ) : (
            <div className="field-value">
              {user.email}
              <button onClick={() => setEditMode({ ...editMode, email: true })}>Изменить</button>
            </div>
          )}
        </div>

        {/* Пароль */}
        <div className="profile-field">
          <label>Пароль</label>
          {editMode.password ? (
            <div className="edit-form">
              <input
                type="password"
                placeholder="Старый пароль"
                value={formData.oldPassword || ''}
                onChange={(e) => setFormData({ ...formData, oldPassword: e.target.value })}
                required
              />
              <input
                type="password"
                placeholder="Новый пароль"
                value={formData.newPassword || ''}
                onChange={(e) => setFormData({ ...formData, newPassword: e.target.value })}
                minLength={6}
                required
              />
              <input
                type="password"
                placeholder="Подтвердите новый пароль"
                value={formData.confirmPassword || ''}
                onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                required
              />
              <div className="edit-actions">
                <button onClick={() => handleEditSubmit('password')}>Сохранить</button>
                <button onClick={() => setEditMode({ ...editMode, password: false })}>Отмена</button>
              </div>
            </div>
          ) : (
            <div className="field-value">
              ••••••••
              <button onClick={() => setEditMode({ ...editMode, password: true })}>Сменить</button>
            </div>
          )}
        </div>
      </section>

      {/* Кошелёк */}
      <section className="profile-card">
        <h2>Кошелёк</h2>
        <div className="balance">
          <span className="balance-amount">
            {balance !== null ? `${balance} tokens` : 'Загрузка...'}
          </span>
        </div>
        <button onClick={() => navigate('/wallet')}>История операций</button>
      </section>
    </div>
  );
}
