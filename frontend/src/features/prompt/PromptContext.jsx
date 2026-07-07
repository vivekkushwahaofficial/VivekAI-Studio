import { createContext, useContext, useState, useEffect } from 'react';
import api from '../../services/api';
import { useAuth } from '../auth/AuthContext';

const PromptContext = createContext(undefined);

export const PromptProvider = ({ children }) => {
  const { token } = useAuth();
  const [profiles, setProfiles] = useState([]);
  const [favorites, setFavorites] = useState([]);
  const categories = [
    'DEVELOPMENT', 'WRITING', 'ANALYSIS', 'EDUCATION', 'INTERVIEW', 'CODING', 'BUSINESS', 'RESEARCH', 'GENERAL', 'PRODUCTIVITY'
  ];
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (token) {
      loadFavorites();
      searchProfiles();
    }
  }, [token]);

  const loadFavorites = async () => {
    try {
      const response = await api.get('/prompts/favorites');
      setFavorites(response.data.data);
    } catch (err) {
      console.error('Failed to load favorite prompt profiles', err);
    }
  };

  const searchProfiles = async (name, category, tag) => {
    setLoading(true);
    try {
      let url = '/prompts/search?';
      if (name) url += `name=${encodeURIComponent(name)}&`;
      if (category) url += `category=${encodeURIComponent(category)}&`;
      if (tag) url += `tag=${encodeURIComponent(tag)}&`;
      
      const response = await api.get(url);
      setProfiles(response.data.data);
    } catch (err) {
      console.error('Failed to search prompt profiles', err);
    } finally {
      setLoading(false);
    }
  };

  const createProfile = async (request) => {
    const response = await api.post('/prompts', request);
    const newProfile = response.data.data;
    setProfiles((prev) => [newProfile, ...prev]);
    return newProfile;
  };

  const toggleFavorite = async (id, isFav) => {
    try {
      if (isFav) {
        await api.post(`/prompts/${id}/favorite`);
      } else {
        await api.delete(`/prompts/${id}/favorite`);
      }
      await loadFavorites();
    } catch (err) {
      console.error('Failed to toggle favorite on prompt profile', err);
    }
  };

  return (
    <PromptContext.Provider
      value={{
        profiles,
        favorites,
        categories,
        loading,
        loadFavorites,
        searchProfiles,
        createProfile,
        toggleFavorite,
      }}
    >
      {children}
    </PromptContext.Provider>
  );
};

export const usePrompt = () => {
  const context = useContext(PromptContext);
  if (!context) {
    throw new Error('usePrompt must be used within a PromptProvider');
  }
  return context;
};
