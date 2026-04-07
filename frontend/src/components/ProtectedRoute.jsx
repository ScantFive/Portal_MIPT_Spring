import { Navigate, Outlet } from 'react-router-dom';
import { auth } from '../api/client';


export default function ProtectedRoute() {
  if (!auth.isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }
  
  return <Outlet />;
}
