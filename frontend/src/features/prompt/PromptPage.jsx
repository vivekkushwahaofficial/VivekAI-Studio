import { useState } from 'react';
import { usePrompt } from './PromptContext';
import { Star, Search, Plus, X } from 'lucide-react';

export const PromptPage = () => {
  const { profiles, favorites, categories, createProfile, toggleFavorite, searchProfiles } = usePrompt();
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);

  // Form states
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [systemPrompt, setSystemPrompt] = useState('');
  const [categoryName, setCategoryName] = useState('DEVELOPMENT');
  const [providerCode, setProviderCode] = useState('GEMINI');
  const [modelName, setModelName] = useState('gemini-1.5-flash');
  const [variables, setVariables] = useState([]);

  // Temp var state
  const [varName, setVarName] = useState('');
  const [varDesc, setVarDesc] = useState('');
  const [varType, setVarType] = useState('STRING');
  const [varDefault, setVarDefault] = useState('');

  const handleSearch = (e) => {
    e.preventDefault();
    searchProfiles(searchTerm, selectedCategory);
  };

  const handleCategorySelect = (cat) => {
    const nextCat = selectedCategory === cat ? '' : cat;
    setSelectedCategory(nextCat);
    searchProfiles(searchTerm, nextCat);
  };

  const addVariable = () => {
    if (!varName) return;
    setVariables([...variables, {
      name: varName,
      description: varDesc,
      type: varType,
      defaultValue: varDefault,
      required: true
    }]);
    setVarName('');
    setVarDesc('');
    setVarDefault('');
  };

  const removeVariable = (idx) => {
    setVariables(variables.filter((_, i) => i !== idx));
  };

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    try {
      await createProfile({
        name,
        description,
        providerCode,
        modelName,
        categoryName,
        systemPrompt,
        icon: 'terminal',
        variables
      });
      setShowCreateModal(false);
      setName('');
      setDescription('');
      setSystemPrompt('');
      setVariables([]);
    } catch (err) {
      console.error(err);
      alert('Failed to save prompt profile');
    }
  };

  return (
    <div className="flex-1 bg-slate-950 p-6 overflow-y-auto space-y-6">
      <div className="flex items-center justify-between">
        <div className="space-y-1">
          <h1 className="text-2xl font-bold text-slate-100 font-outfit">Prompt Library</h1>
          <p className="text-sm text-slate-400">Discover, search, and deploy reusable AI template profiles</p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="flex items-center gap-1.5 bg-cyan-500 hover:bg-cyan-400 text-slate-950 px-4 py-2.5 rounded-xl font-semibold transition-all text-sm"
        >
          <Plus className="w-4 h-4" />
          <span>New Profile</span>
        </button>
      </div>

      <div className="space-y-4">
        <form onSubmit={handleSearch} className="flex gap-2">
          <div className="flex-1 relative">
            <Search className="absolute left-3.5 top-3 w-4 h-4 text-slate-500" />
            <input
              type="text"
              placeholder="Search prompts by name or keyword..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full bg-slate-900 border border-slate-800/80 rounded-xl pl-10 pr-4 py-2.5 text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500/50 text-sm"
            />
          </div>
          <button
            type="submit"
            className="bg-slate-900 border border-slate-800 hover:border-slate-700 text-slate-200 px-5 py-2.5 rounded-xl text-sm font-semibold transition-colors"
          >
            Search
          </button>
        </form>

        <div className="flex gap-1.5 overflow-x-auto pb-2 scrollbar-none">
          {categories.map((cat) => (
            <button
              key={cat}
              onClick={() => handleCategorySelect(cat)}
              className={`px-3.5 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap border transition-all ${
                selectedCategory === cat
                  ? 'bg-cyan-500/10 border-cyan-500/40 text-cyan-400'
                  : 'bg-slate-900/60 border-slate-800/70 text-slate-400 hover:border-slate-700'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {profiles.map((profile) => {
          const isFav = favorites.some((f) => f.id === profile.id);
          return (
            <div
              key={profile.id}
              className="bg-slate-900/40 border border-slate-800/80 rounded-2xl p-5 hover:border-slate-700/50 transition-all flex flex-col justify-between space-y-4 relative"
            >
              <button
                onClick={() => toggleFavorite(profile.id, !isFav)}
                className="absolute top-4 right-4 text-slate-400 hover:text-amber-400 transition-colors"
              >
                <Star className={`w-4 h-4 ${isFav ? 'fill-amber-400 text-amber-400' : ''}`} />
              </button>

              <div className="space-y-2">
                <div className="inline-flex items-center gap-1 bg-cyan-950/40 border border-cyan-800/30 px-2 py-0.5 rounded text-[10px] font-bold text-cyan-400">
                  {profile.categoryName || 'GENERAL'}
                </div>
                <h3 className="font-bold text-slate-200 text-base">{profile.name}</h3>
                <p className="text-xs text-slate-400 line-clamp-2">{profile.description}</p>
              </div>

              <div className="pt-3 border-t border-slate-900 flex items-center justify-between text-xs text-slate-500">
                <span>Model: {profile.modelName}</span>
                <span className="font-semibold text-cyan-400">{profile.providerName}</span>
              </div>
            </div>
          );
        })}
      </div>

      {showCreateModal && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4 z-50 overflow-y-auto">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 w-full max-w-xl max-h-[90vh] overflow-y-auto space-y-6 relative">
            <button
              onClick={() => setShowCreateModal(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-200"
            >
              <X className="w-5 h-5" />
            </button>

            <h2 className="text-xl font-bold text-slate-100 font-outfit">Create Prompt Profile</h2>

            <form onSubmit={handleSaveProfile} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1">
                  <label className="text-xs text-slate-400 font-semibold">Name</label>
                  <input
                    type="text"
                    required
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-slate-200 text-sm focus:outline-none focus:border-cyan-500/50"
                    placeholder="e.g. Java Coach"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-xs text-slate-400 font-semibold">Category</label>
                  <select
                    value={categoryName}
                    onChange={(e) => setCategoryName(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-slate-200 text-sm focus:outline-none focus:border-cyan-500/50"
                  >
                    {categories.map((c) => (
                      <option key={c} value={c}>{c}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="space-y-1">
                <label className="text-xs text-slate-400 font-semibold">Description</label>
                <input
                  type="text"
                  required
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-slate-200 text-sm focus:outline-none focus:border-cyan-500/50"
                  placeholder="e.g. Senior mentor code reviewers"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1">
                  <label className="text-xs text-slate-400 font-semibold">AI Provider</label>
                  <select
                    value={providerCode}
                    onChange={(e) => setProviderCode(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-slate-200 text-sm focus:outline-none"
                  >
                    <option value="GEMINI">GEMINI</option>
                    <option value="OPENAI">OPENAI</option>
                    <option value="CLAUDE">CLAUDE</option>
                    <option value="DEEPSEEK">DEEPSEEK</option>
                    <option value="OLLAMA">OLLAMA</option>
                  </select>
                </div>
                <div className="space-y-1">
                  <label className="text-xs text-slate-400 font-semibold">Model Name</label>
                  <input
                    type="text"
                    required
                    value={modelName}
                    onChange={(e) => setModelName(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-slate-200 text-sm focus:outline-none"
                  />
                </div>
              </div>

              <div className="space-y-1">
                <label className="text-xs text-slate-400 font-semibold">System Prompt Template</label>
                <textarea
                  required
                  value={systemPrompt}
                  onChange={(e) => setSystemPrompt(e.target.value)}
                  rows={4}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-slate-200 text-sm focus:outline-none font-mono"
                  placeholder="You are {{name}} coaching {{language}}..."
                />
              </div>

              <div className="space-y-2 pt-2 border-t border-slate-800/80">
                <label className="text-xs font-bold text-slate-300">Variables Schema Config</label>
                
                <div className="flex gap-2 items-end">
                  <div className="flex-1 space-y-1">
                    <span className="text-[10px] text-slate-500 font-semibold">Var Name</span>
                    <input
                      type="text"
                      value={varName}
                      onChange={(e) => setVarName(e.target.value)}
                      className="w-full bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-slate-200 focus:outline-none"
                      placeholder="e.g. language"
                    />
                  </div>
                  <div className="flex-1 space-y-1">
                    <span className="text-[10px] text-slate-500 font-semibold">Type</span>
                    <select
                      value={varType}
                      onChange={(e) => setVarType(e.target.value)}
                      className="w-full bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-slate-200"
                    >
                      <option value="STRING">STRING</option>
                      <option value="SELECT">SELECT</option>
                      <option value="BOOLEAN">BOOLEAN</option>
                    </select>
                  </div>
                  <div className="flex-1 space-y-1">
                    <span className="text-[10px] text-slate-500 font-semibold">Default</span>
                    <input
                      type="text"
                      value={varDefault}
                      onChange={(e) => setVarDefault(e.target.value)}
                      className="w-full bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-slate-200 focus:outline-none"
                      placeholder="e.g. Java"
                    />
                  </div>
                  <button
                    type="button"
                    onClick={addVariable}
                    className="bg-cyan-500 hover:bg-cyan-400 text-slate-950 text-xs font-semibold px-3 py-2 rounded-lg"
                  >
                    Add
                  </button>
                </div>

                <div className="flex flex-wrap gap-1.5 pt-2">
                  {variables.map((v, i) => (
                    <div key={i} className="flex items-center gap-1 bg-slate-950 border border-slate-800 px-2.5 py-1 rounded-lg text-xs text-slate-300">
                      <span>{v.name} ({v.type})</span>
                      <button type="button" onClick={() => removeVariable(i)} className="text-slate-500 hover:text-slate-300">
                        <X className="w-3 h-3" />
                      </button>
                    </div>
                  ))}
                </div>
              </div>

              <button
                type="submit"
                className="w-full bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold py-2.5 rounded-xl text-sm transition-all"
              >
                Save Profile
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
