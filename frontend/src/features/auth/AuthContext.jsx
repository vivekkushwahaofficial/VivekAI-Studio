import { createContext, useContext, useState, useEffect } from 'react';
import api, { setAccessToken } from '../../services/api';

const AuthContext = createContext(undefined);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  const updateToken = (newToken) => {
    setToken(newToken);
    setAccessToken(newToken);
  };

  useEffect(() => {
    const initializeAuth = async () => {
      try {
        // Attempt session recovery by requesting token rotation via HttpOnly refresh cookie
        const response = await api.post('/auth/refresh');
        const { accessToken, user: userData } = response.data.data;
        updateToken(accessToken);
        setUser(userData);
      } catch (error) {
        updateToken(null);
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    initializeAuth();
  }, []);

  const login = async (username, password) => {
    const response = await api.post('/auth/login', { username, password });
    const { accessToken, user: userData } = response.data.data;
    updateToken(accessToken);
    setUser(userData);
  };

  const register = async (username, email, password) => {
    await api.post('/auth/register', { username, email, password });
  };

  const logout = async () => {
    try {
      await api.post('/auth/logout');
    } catch (err) {
      console.error('API logout notification failed', err);
    } finally {
      updateToken(null);
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider value={{ user, token, isAuthenticated: !!token, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
