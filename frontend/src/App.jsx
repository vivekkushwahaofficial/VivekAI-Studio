import { useState } from 'react';
import { AuthProvider, useAuth } from './features/auth/AuthContext';
import { WorkspaceProvider } from './features/workspace/WorkspaceContext';
import { PromptProvider } from './features/prompt/PromptContext';
import { AuthPage } from './features/auth/AuthPage';
import { ChatPage } from './features/chat/ChatPage';
import { PromptPage } from './features/prompt/PromptPage';
import { SettingsPage } from './features/settings/SettingsPage';
import { MessageSquare, Terminal, Settings, Sparkles } from 'lucide-react';

function AppContent() {
  const { isAuthenticated, loading } = useAuth();
  const [activeTab, setActiveTab] = useState('chat');

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-cyan-400"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <AuthPage />;
  }

  return (
    <WorkspaceProvider>
      <PromptProvider>
        <div className="h-screen flex flex-col bg-slate-950 text-slate-100 overflow-hidden">
          <header className="h-14 border-b border-slate-900 bg-slate-950 px-6 flex items-center justify-between shrink-0 z-20">
            <div className="flex items-center gap-2">
              <Sparkles className="w-5 h-5 text-cyan-400" />
              <span className="font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-indigo-500 font-outfit">
                VivekAI Studio
              </span>
            </div>
            
            <nav className="flex items-center gap-2">
              <button
                onClick={() => setActiveTab('chat')}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold border transition-all ${
                  activeTab === 'chat'
                    ? 'bg-cyan-500/10 border-cyan-500/30 text-cyan-400'
                    : 'border-transparent text-slate-400 hover:text-slate-200'
                }`}
              >
                <MessageSquare className="w-3.5 h-3.5" />
                <span>Chat</span>
              </button>
              
              <button
                onClick={() => setActiveTab('library')}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold border transition-all ${
                  activeTab === 'library'
                    ? 'bg-cyan-500/10 border-cyan-500/30 text-cyan-400'
                    : 'border-transparent text-slate-400 hover:text-slate-200'
                }`}
              >
                <Terminal className="w-3.5 h-3.5" />
                <span>Prompt Library</span>
              </button>

              <button
                onClick={() => setActiveTab('settings')}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold border transition-all ${
                  activeTab === 'settings'
                    ? 'bg-cyan-500/10 border-cyan-500/30 text-cyan-400'
                    : 'border-transparent text-slate-400 hover:text-slate-200'
                }`}
              >
                <Settings className="w-3.5 h-3.5" />
                <span>Settings</span>
              </button>
            </nav>
          </header>

          <div className="flex-1 flex overflow-hidden">
            {activeTab === 'chat' && <ChatPage />}
            {activeTab === 'library' && <PromptPage />}
            {activeTab === 'settings' && <SettingsPage />}
          </div>
        </div>
      </PromptProvider>
    </WorkspaceProvider>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}
