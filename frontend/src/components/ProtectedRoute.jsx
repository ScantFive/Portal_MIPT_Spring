import { Navigate, Outlet } from 'react-router-dom';
import { auth } from '../api/client';

/**
 * Компонент-обёртка для защиты маршрутов.
 * Если пользователь не авторизован — редирект на /login
 */
export default function ProtectedRoute() {
  if (!auth.isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }
  
  // Если авторизован — рендерим вложенные маршруты
  return <Outlet />;
}
