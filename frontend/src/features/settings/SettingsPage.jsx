import { useAuth } from '../auth/AuthContext';
import { Key, User } from 'lucide-react';

export const SettingsPage = () => {
  const { user, logout } = useAuth();

  return (
    <div className="flex-1 bg-slate-950 p-6 overflow-y-auto space-y-6">
      <div className="space-y-1">
        <h1 className="text-2xl font-bold text-slate-100 font-outfit">Settings</h1>
        <p className="text-sm text-slate-400">Manage your profiles, API provider configurations, and themes</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="bg-slate-900/40 border border-slate-800/80 rounded-2xl p-6 space-y-4">
          <div className="flex items-center gap-3">
            <div className="bg-cyan-500/10 p-2.5 rounded-xl border border-cyan-500/20 text-cyan-400">
              <User className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-slate-200">User Profile</h3>
              <p className="text-xs text-slate-400">Your account parameters</p>
            </div>
          </div>

          <div className="space-y-3 pt-2 text-sm">
            <div className="flex justify-between">
              <span className="text-slate-500">Username</span>
              <span className="text-slate-300 font-medium">{user?.username || 'user'}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-500">Email</span>
              <span className="text-slate-300 font-medium">{user?.email || 'email@workspace.com'}</span>
            </div>
          </div>

          <button
            onClick={logout}
            className="w-full bg-slate-950 border border-rose-900/50 hover:bg-rose-950/20 hover:border-rose-700/60 text-rose-400 font-semibold py-2 rounded-xl transition-all text-xs"
          >
            Sign Out
          </button>
        </div>

        <div className="bg-slate-900/40 border border-slate-800/80 rounded-2xl p-6 space-y-4 lg:col-span-2">
          <div className="flex items-center gap-3">
            <div className="bg-cyan-500/10 p-2.5 rounded-xl border border-cyan-500/20 text-cyan-400">
              <Key className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-slate-200">AI Credentials</h3>
              <p className="text-xs text-slate-400">Access tokens are securely processed backend-side</p>
            </div>
          </div>

          <div className="space-y-4 pt-2 text-xs">
            <div className="flex items-center justify-between p-3 bg-slate-950/60 border border-slate-850 rounded-xl">
              <div className="space-y-0.5">
                <span className="font-bold text-slate-300">Offline Mock Simulation Mode</span>
                <p className="text-slate-500 text-[10px]">Active automatically when API keys are not supplied</p>
              </div>
              <span className="px-2 py-0.5 rounded bg-cyan-950 text-cyan-400 font-bold border border-cyan-800/30 text-[10px]">
                ACTIVE
              </span>
            </div>

            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-slate-400 font-medium">OpenAI Key</span>
                <span className="text-slate-600 font-mono">••••••••••••••••</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-slate-400 font-medium">Gemini Key</span>
                <span className="text-slate-600 font-mono">••••••••••••••••</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-slate-400 font-medium">Claude Key</span>
                <span className="text-slate-600 font-mono">••••••••••••••••</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
