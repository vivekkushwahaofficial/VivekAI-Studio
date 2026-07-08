import { createContext, useContext, useState, useEffect } from 'react';
import api from '../../services/api';
import { useAuth } from '../auth/AuthContext';
import { fetchEventSource } from '@microsoft/fetch-event-source';

const WorkspaceContext = createContext(undefined);

export const WorkspaceProvider = ({ children }) => {
  const { token } = useAuth();
  const [workspaces, setWorkspaces] = useState([]);
  const [activeWorkspace, setActiveWorkspace] = useState(null);
  const [folders, setFolders] = useState([]);
  const [conversations, setConversations] = useState([]);
  const [activeConversation, setActiveConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);

  // SSE states
  const [isStreaming, setIsStreaming] = useState(false);
  const [streamingMessage, setStreamingMessage] = useState('');

  // Load workspaces on init
  useEffect(() => {
    if (token) {
      loadWorkspaces();
    }
  }, [token]);

  // Load folders & conversations when active workspace changes
  useEffect(() => {
    if (activeWorkspace) {
      loadFolders(activeWorkspace.id);
      loadConversations(activeWorkspace.id);
      setActiveConversation(null);
      setMessages([]);
    }
  }, [activeWorkspace]);

  // Load messages when active conversation changes
  useEffect(() => {
    if (activeConversation) {
      loadMessages(activeConversation.id);
    } else {
      setMessages([]);
    }
  }, [activeConversation]);

  const loadWorkspaces = async () => {
    setLoading(true);
    try {
      const response = await api.get('/workspaces');
      setWorkspaces(response.data.data);
      if (response.data.data.length > 0 && !activeWorkspace) {
        setActiveWorkspace(response.data.data[0]);
      }
    } catch (err) {
      console.error('Failed to load workspaces', err);
    } finally {
      setLoading(false);
    }
  };

  const selectWorkspace = (workspace) => {
    setActiveWorkspace(workspace);
  };

  const createWorkspace = async (name, description) => {
    try {
      const response = await api.post('/workspaces', { name, description });
      const newWs = response.data.data;
      setWorkspaces((prev) => [...prev, newWs]);
      setActiveWorkspace(newWs);
    } catch (err) {
      console.error('Failed to create workspace', err);
    }
  };

  const loadFolders = async (workspaceId) => {
    try {
      const response = await api.get(`/workspaces/${workspaceId}/folders`);
      setFolders(response.data.data);
    } catch (err) {
      console.error('Failed to load folders', err);
    }
  };

  const createFolder = async (name) => {
    if (!activeWorkspace) return;
    try {
      const response = await api.post(`/workspaces/${activeWorkspace.id}/folders`, { name });
      setFolders((prev) => [...prev, response.data.data]);
    } catch (err) {
      console.error('Failed to create folder', err);
    }
  };

  const loadConversations = async (workspaceId) => {
    try {
      const response = await api.get(`/conversations/workspace/${workspaceId}`);
      setConversations(response.data.data);
    } catch (err) {
      console.error('Failed to load conversations', err);
    }
  };

  const loadMessages = async (conversationId) => {
    try {
      const response = await api.get(`/conversations/${conversationId}/messages`);
      setMessages(response.data.data);
    } catch (err) {
      console.error('Failed to load messages', err);
    }
  };

  const selectConversation = (conversation) => {
    setActiveConversation(conversation);
  };

  const createConversation = () => {
    setActiveConversation(null);
    setMessages([]);
  };

  const togglePin = async (id, pin) => {
    try {
      await api.patch(`/conversations/${id}/pin?pin=${pin}`);
      setConversations((prev) =>
        prev.map((c) => (c.id === id ? { ...c, pinned: pin } : c))
      );
      if (activeConversation?.id === id) {
        setActiveConversation((prev) => prev ? { ...prev, pinned: pin } : null);
      }
    } catch (err) {
      console.error('Failed to toggle pin', err);
    }
  };

  const toggleFavorite = async (id, favorite) => {
    try {
      await api.patch(`/conversations/${id}/favorite?favorite=${favorite}`);
      setConversations((prev) =>
        prev.map((c) => (c.id === id ? { ...c, favorite } : c))
      );
      if (activeConversation?.id === id) {
        setActiveConversation((prev) => prev ? { ...prev, favorite } : null);
      }
    } catch (err) {
      console.error('Failed to toggle favorite', err);
    }
  };

  const deleteConversation = async (id) => {
    try {
      await api.delete(`/conversations/${id}`);
      setConversations((prev) => prev.filter((c) => c.id !== id));
      if (activeConversation?.id === id) {
        setActiveConversation(null);
      }
    } catch (err) {
      console.error('Failed to delete conversation', err);
    }
  };

  const sendMessage = async (
    prompt,
    provider,
    model,
    variables,
    promptProfileId
  ) => {
    if (!activeWorkspace) return;

    const tempUserMsg = {
      id: Math.random().toString(),
      role: 'USER',
      content: prompt,
      status: 'SUCCESS',
      createdAt: new Date().toISOString(),
    };
    setMessages((prev) => [...prev, tempUserMsg]);

    setIsStreaming(true);
    setStreamingMessage('');

    const variablesParam = variables
      ? Object.entries(variables)
          .map(([k, v]) => `variables[${k}]=${encodeURIComponent(v)}`)
          .join('&')
      : '';

    let url = `/api/v1/chat/${activeWorkspace.id}/stream?prompt=${encodeURIComponent(prompt)}&providerCode=${provider}&modelName=${model}`;
    if (activeConversation) {
      url += `&conversationId=${activeConversation.id}`;
    }
    if (promptProfileId) {
      url += `&promptProfileId=${promptProfileId}`;
    }
    if (variablesParam) {
      url += `&${variablesParam}`;
    }

    const ctrl = new AbortController();

    const addErrorMessageToChat = () => {
      const tempErrorMsg = {
        id: Math.random().toString(),
        role: 'ASSISTANT',
        content: 'Error: Stream generation interrupted. Please try again.',
        status: 'FAILED',
        createdAt: new Date().toISOString(),
      };
      setMessages((prev) => [...prev, tempErrorMsg]);
    };

    fetchEventSource(url, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      signal: ctrl.signal,
      async onopen(response) {
        if (response.ok && response.headers.get('content-type')?.includes('text/event-stream')) {
          // Response is valid event stream
        } else if (response.status >= 400 && response.status < 500 && response.status !== 429) {
          throw new Error("Client streaming request failed: " + response.statusText);
        }
      },
      onmessage(msg) {
        if (msg.event === 'START') {
          setStreamingMessage('');
        } else if (msg.event === 'TOKEN') {
          try {
            const data = JSON.parse(msg.data);
            if (data && data.content) {
              setStreamingMessage((prev) => prev + data.content);
            }
          } catch (e) {
            console.error('Failed to parse token data', e);
          }
        } else if (msg.event === 'FINISH') {
          setIsStreaming(false);
          setStreamingMessage('');
          ctrl.abort();

          loadConversations(activeWorkspace.id).then(() => {
            if (!activeConversation) {
              api.get(`/conversations/workspace/${activeWorkspace.id}`).then((res) => {
                const list = res.data.data;
                if (list.length > 0) {
                  setActiveConversation(list[0]);
                }
              });
            } else {
              loadMessages(activeConversation.id);
            }
          });
        } else if (msg.event === 'ERROR') {
          console.error('SSE error event received', msg.data);
          setIsStreaming(false);
          ctrl.abort();
          addErrorMessageToChat();
        }
      },
      onclose() {
        setIsStreaming(false);
      },
      onerror(err) {
        console.error('SSE request failed', err);
        setIsStreaming(false);
        ctrl.abort();
        addErrorMessageToChat();
        throw err; // Stop retry loops
      }
    });
  };

  return (
    <WorkspaceContext.Provider
      value={{
        workspaces,
        activeWorkspace,
        folders,
        conversations,
        activeConversation,
        messages,
        loading,
        isStreaming,
        streamingMessage,
        loadWorkspaces,
        selectWorkspace,
        createWorkspace,
        createFolder,
        selectConversation,
        createConversation,
        sendMessage,
        togglePin,
        toggleFavorite,
        deleteConversation,
      }}
    >
      {children}
    </WorkspaceContext.Provider>
  );
};

export const useWorkspace = () => {
  const context = useContext(WorkspaceContext);
  if (!context) {
    throw new Error('useWorkspace must be used within a WorkspaceProvider');
  }
  return context;
};
