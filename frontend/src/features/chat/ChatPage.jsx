import { useState, useEffect, useRef } from 'react';
import { useWorkspace } from '../workspace/WorkspaceContext';
import { usePrompt } from '../prompt/PromptContext';
import { MarkdownRenderer } from '../../components/MarkdownRenderer';
import { 
  Sparkles, Pin, Star, Trash2, Send, FolderPlus, 
  MessageSquare, ChevronDown, Bot
} from 'lucide-react';

export const ChatPage = () => {
  const {
    workspaces,
    activeWorkspace,
    folders,
    conversations,
    activeConversation,
    messages,
    isStreaming,
    streamingMessage,
    selectWorkspace,
    createWorkspace,
    createFolder,
    selectConversation,
    sendMessage,
    togglePin,
    toggleFavorite,
    deleteConversation
  } = useWorkspace();

  const { profiles } = usePrompt();

  const [provider, setProvider] = useState('GEMINI');
  const [model, setModel] = useState('gemini-1.5-flash');
  const [activeProfile, setActiveProfile] = useState(null);
  
  const [varInputs, setVarInputs] = useState({});

  const [prompt, setPrompt] = useState('');
  const messagesEndRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, streamingMessage]);

  useEffect(() => {
    if (provider === 'GEMINI') setModel('gemini-1.5-flash');
    else if (provider === 'OPENAI') setModel('gpt-4o');
    else if (provider === 'CLAUDE') setModel('claude-3-5-sonnet');
    else if (provider === 'DEEPSEEK') setModel('deepseek-chat');
    else if (provider === 'OLLAMA') setModel('llama3');
  }, [provider]);

  useEffect(() => {
    if (activeProfile) {
      setProvider(activeProfile.providerName.toUpperCase());
      setModel(activeProfile.modelName);
      
      const inputs = {};
      activeProfile.variables.forEach(v => {
        inputs[v.name] = v.defaultValue || '';
      });
      setVarInputs(inputs);
    } else {
      setVarInputs({});
    }
  }, [activeProfile]);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!prompt.trim()) return;

    const currentPrompt = prompt;
    setPrompt('');

    await sendMessage(
      currentPrompt,
      provider,
      model,
      varInputs,
      activeProfile?.id
    );
  };

  const handleCreateFolder = () => {
    const name = window.prompt('Enter folder name:');
    if (name) createFolder(name);
  };

  const handleCreateWorkspace = () => {
    const name = window.prompt('Enter workspace name:');
    const desc = window.prompt('Enter description:');
    if (name) createWorkspace(name, desc || '');
  };

  return (
    <div className="flex-1 flex bg-slate-950 overflow-hidden text-slate-100">
      <div className="w-64 border-r border-slate-900 bg-slate-950 flex flex-col justify-between shrink-0">
        <div className="flex-1 flex flex-col overflow-y-auto">
          <div className="p-4 border-b border-slate-900 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-500 tracking-wider">WORKSPACE</span>
              <button onClick={handleCreateWorkspace} className="text-[10px] text-cyan-400 hover:underline">
                + Add
              </button>
            </div>
            <div className="relative group">
              <select
                value={activeWorkspace?.id || ''}
                onChange={(e) => {
                  const selected = workspaces.find(w => w.id === e.target.value);
                  if (selected) selectWorkspace(selected);
                }}
                className="w-full bg-slate-900 border border-slate-800 rounded-xl px-3 py-2 text-xs font-semibold text-slate-300 focus:outline-none appearance-none cursor-pointer"
              >
                {workspaces.map(w => (
                  <option key={w.id} value={w.id}>{w.name}</option>
                ))}
              </select>
              <ChevronDown className="w-3.5 h-3.5 absolute right-3 top-2.5 text-slate-500 pointer-events-none" />
            </div>
          </div>

          <div className="p-4 border-b border-slate-900 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-500 tracking-wider">FOLDERS</span>
              <button onClick={handleCreateFolder} className="text-slate-500 hover:text-slate-300">
                <FolderPlus className="w-3.5 h-3.5" />
              </button>
            </div>
            <div className="space-y-1 text-xs text-slate-400">
              {folders.map(f => (
                <div key={f.id} className="flex items-center gap-2 px-2 py-1.5 rounded-lg hover:bg-slate-900/40">
                  <span>📁</span>
                  <span>{f.name}</span>
                </div>
              ))}
              {folders.length === 0 && (
                <div className="text-[10px] text-slate-600 italic px-2">No folders configured</div>
              )}
            </div>
          </div>

          <div className="flex-1 p-4 space-y-2 overflow-y-auto">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-500 tracking-wider">CHAT HISTORY</span>
              <button onClick={() => selectConversation(null)} className="text-[10px] text-cyan-400 hover:underline">
                + New Chat
              </button>
            </div>

            <div className="space-y-1">
              {conversations.map(c => {
                const isActive = activeConversation?.id === c.id;
                return (
                  <div
                    key={c.id}
                    onClick={() => selectConversation(c)}
                    className={`group flex items-center justify-between px-3 py-2 rounded-xl text-xs cursor-pointer border transition-colors ${
                      isActive 
                        ? 'bg-slate-900 border-slate-800 text-cyan-400' 
                        : 'border-transparent text-slate-400 hover:bg-slate-900/30'
                    }`}
                  >
                    <div className="flex items-center gap-2 truncate">
                      <MessageSquare className="w-3.5 h-3.5 shrink-0" />
                      <span className="truncate">{c.title}</span>
                    </div>

                    <div className="hidden group-hover:flex items-center gap-1.5 shrink-0">
                      <button 
                        onClick={(e) => { e.stopPropagation(); togglePin(c.id, !c.pinned); }}
                        className={`hover:text-slate-200 ${c.pinned ? 'text-amber-400' : ''}`}
                      >
                        <Pin className="w-3 h-3" />
                      </button>
                      <button 
                        onClick={(e) => { e.stopPropagation(); toggleFavorite(c.id, !c.favorite); }}
                        className={`hover:text-slate-200 ${c.favorite ? 'text-amber-400' : ''}`}
                      >
                        <Star className="w-3 h-3" />
                      </button>
                      <button 
                        onClick={(e) => { e.stopPropagation(); deleteConversation(c.id); }}
                        className="hover:text-rose-400"
                      >
                        <Trash2 className="w-3 h-3" />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </div>

      <div className="flex-1 flex flex-col bg-slate-950 justify-between overflow-hidden relative">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,rgba(6,182,212,0.03),transparent_70%)] pointer-events-none" />

        <div className="px-6 py-4 border-b border-slate-900 flex items-center justify-between z-10 bg-slate-950/80 backdrop-blur-xl">
          <div className="flex items-center gap-2">
            <Bot className="w-5 h-5 text-cyan-400" />
            <h2 className="font-bold text-sm text-slate-200">
              {activeConversation ? activeConversation.title : 'New Chat Session'}
            </h2>
          </div>

          <div className="flex items-center gap-3">
            <select
              value={provider}
              onChange={(e) => setProvider(e.target.value)}
              className="bg-slate-900 border border-slate-800 text-xs rounded-lg px-2.5 py-1 focus:outline-none"
            >
              <option value="GEMINI">Gemini</option>
              <option value="OPENAI">OpenAI</option>
              <option value="CLAUDE">Claude</option>
              <option value="DEEPSEEK">DeepSeek</option>
              <option value="OLLAMA">Ollama (Local)</option>
            </select>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto px-6 py-6 space-y-6 z-10">
          {messages.length === 0 && !isStreaming && (
            <div className="max-w-2xl mx-auto text-center space-y-6 pt-12">
              <Sparkles className="w-10 h-10 text-cyan-400 mx-auto animate-pulse" />
              <div className="space-y-2">
                <h2 className="text-xl font-bold font-outfit text-slate-200">Select Prompt Profile</h2>
                <p className="text-xs text-slate-500">Pick a pre-configured assistant from your libraries to bootstrap context:</p>
              </div>

              <div className="grid grid-cols-2 gap-3 mt-4">
                {profiles.map(p => (
                  <div
                    key={p.id}
                    onClick={() => setActiveProfile(p)}
                    className={`p-3.5 border rounded-2xl text-left cursor-pointer transition-colors ${
                      activeProfile?.id === p.id 
                        ? 'bg-cyan-500/10 border-cyan-500/40 text-cyan-400' 
                        : 'bg-slate-900/30 border-slate-800/80 text-slate-400 hover:border-slate-700'
                    }`}
                  >
                    <span className="font-bold text-xs block text-slate-200">{p.name}</span>
                    <span className="text-[10px] text-slate-500 block truncate">{p.description}</span>
                  </div>
                ))}
              </div>

              {activeProfile && (
                <button 
                  onClick={() => setActiveProfile(null)}
                  className="text-xs text-rose-400 hover:underline pt-2 block mx-auto"
                >
                  Clear selected profile
                </button>
              )}
            </div>
          )}

          {messages.map((msg) => {
            const isUser = msg.role === 'USER';
            return (
              <div key={msg.id} className={`flex gap-4 max-w-3xl mx-auto ${isUser ? 'justify-end' : ''}`}>
                {!isUser && (
                  <div className="w-8 h-8 rounded-full bg-cyan-950 border border-cyan-800/30 flex items-center justify-center text-cyan-400 text-xs shrink-0">
                    <Bot className="w-4 h-4" />
                  </div>
                )}
                
                <div className={`p-4 rounded-2xl text-sm border space-y-2 ${
                  isUser 
                    ? 'bg-cyan-950/20 border-cyan-500/20 text-slate-200' 
                    : 'bg-slate-900/20 border-slate-850 text-slate-300'
                }`}>
                  <MarkdownRenderer content={msg.content} />
                </div>

                {isUser && (
                  <div className="w-8 h-8 rounded-full bg-indigo-950 border border-indigo-800/30 flex items-center justify-center text-indigo-400 text-xs shrink-0 font-bold">
                    U
                  </div>
                )}
              </div>
            );
          })}

          {isStreaming && streamingMessage && (
            <div className="flex gap-4 max-w-3xl mx-auto">
              <div className="w-8 h-8 rounded-full bg-cyan-950 border border-cyan-800/30 flex items-center justify-center text-cyan-400 text-xs shrink-0">
                <Bot className="w-4 h-4" />
              </div>
              <div className="p-4 rounded-2xl text-sm border bg-slate-900/20 border-slate-850 text-slate-300 flex-1">
                <MarkdownRenderer content={streamingMessage} />
                <span className="inline-block w-1.5 h-4 bg-cyan-400 animate-pulse ml-1 align-middle" />
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {activeProfile && activeProfile.variables.length > 0 && messages.length === 0 && (
          <div className="px-6 py-3 border-t border-slate-900 bg-slate-950 z-20 space-y-2">
            <span className="text-[10px] font-bold text-slate-500 uppercase tracking-wider block">
              Profile Variable Inputs
            </span>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
              {activeProfile.variables.map(v => (
                <div key={v.name} className="space-y-1">
                  <span className="text-[10px] text-slate-400 font-semibold block truncate">
                    {v.name}{v.required && '*'}
                  </span>
                  <input
                    type="text"
                    value={varInputs[v.name] || ''}
                    onChange={(e) => setVarInputs({ ...varInputs, [v.name]: e.target.value })}
                    className="w-full bg-slate-900 border border-slate-800 rounded-lg px-2 py-1 text-xs text-slate-200 focus:outline-none"
                    placeholder={v.description || `Enter ${v.name}`}
                  />
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="p-6 border-t border-slate-900 z-10">
          <form onSubmit={handleSend} className="max-w-3xl mx-auto flex gap-3 relative">
            <input
              type="text"
              placeholder={isStreaming ? 'Stream response executing...' : 'Ask anything, type a message...'}
              disabled={isStreaming}
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              className="flex-1 bg-slate-900 border border-slate-800 rounded-2xl px-5 py-3.5 pr-14 text-sm focus:outline-none focus:border-cyan-500/50 placeholder-slate-500"
            />
            <button
              type="submit"
              disabled={isStreaming || !prompt.trim()}
              className="absolute right-3.5 top-3.5 bg-cyan-500 hover:bg-cyan-400 text-slate-950 p-1.5 rounded-xl transition-all disabled:opacity-50"
            >
              <Send className="w-4 h-4" />
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
