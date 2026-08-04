'use client';

import { useEffect, useState } from 'react';
import { getApiKeys, createApiKey, getUsers, ApiKeyData, UserData, CreatedApiKey } from '@/lib/api-services';

export default function ApiKeysPage() {
  const [keys, setKeys] = useState<ApiKeyData[]>([]);
  const [users, setUsers] = useState<UserData[]>([]);
  const [loading, setLoading] = useState(true);

  const [userId, setUserId] = useState('');
  const [provider, setProvider] = useState('ANTHROPIC');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [newKey, setNewKey] = useState<CreatedApiKey | null>(null);
  const [copied, setCopied] = useState(false);

  const load = () => {
    setLoading(true);
    Promise.all([getApiKeys(), getUsers()])
      .then(([k, u]) => {
        setKeys(k);
        setUsers(u);
      })
      .catch(() => {
        setKeys([]);
        setUsers([]);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setNewKey(null);
    if (!userId) {
      setError('Select a user.');
      return;
    }
    setSubmitting(true);
    try {
      const created = await createApiKey({ userId, provider });
      setNewKey(created);
      setCopied(false);
      setUserId('');
      load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create key');
    } finally {
      setSubmitting(false);
    }
  };

  const copyKey = async () => {
    if (!newKey) return;
    await navigator.clipboard.writeText(newKey.apiKey);
    setCopied(true);
  };

  return (
    <div className="p-8 max-w-7xl mx-auto space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-slate-900 dark:text-slate-100">API Keys</h1>
        <p className="text-slate-500 mt-1">Provision virtual proxy keys for Gemini &amp; Anthropic request authentication.</p>
      </div>

      <form onSubmit={handleCreate} className="p-6 bg-white dark:bg-slate-900 border rounded-xl shadow-sm flex flex-col md:flex-row gap-4 items-end">
        <div className="flex-1 space-y-1">
          <label className="text-sm font-medium text-slate-700 dark:text-slate-300">User</label>
          <select
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            className="w-full px-3 py-2 border rounded-lg dark:bg-slate-800 dark:border-slate-700"
            required
          >
            <option value="">Select a user…</option>
            {users.map((u) => (
              <option key={u.id} value={u.id}>{u.email}{u.fullName ? ` (${u.fullName})` : ''}</option>
            ))}
          </select>
        </div>
        <div className="w-full md:w-48 space-y-1">
          <label className="text-sm font-medium text-slate-700 dark:text-slate-300">Provider</label>
          <select
            value={provider}
            onChange={(e) => setProvider(e.target.value)}
            className="w-full px-3 py-2 border rounded-lg dark:bg-slate-800 dark:border-slate-700"
          >
            <option value="ANTHROPIC">ANTHROPIC</option>
            <option value="GEMINI">GEMINI</option>
          </select>
        </div>
        <button
          type="submit"
          disabled={submitting}
          className="px-5 py-2 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 transition disabled:opacity-50"
        >
          {submitting ? 'Generating…' : 'Generate Key'}
        </button>
      </form>

      {(users.length === 0 && !loading) && (
        <p className="text-sm text-amber-600">Create a user first before provisioning a key.</p>
      )}
      {error && <p className="text-sm text-red-600">{error}</p>}

      {newKey && (
        <div className="p-5 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-300 dark:border-emerald-800 rounded-xl space-y-3">
          <div className="flex items-center justify-between">
            <p className="text-sm font-semibold text-emerald-800 dark:text-emerald-300">
              Key created for {newKey.userEmail} ({newKey.provider}) — copy it now, it won&apos;t be shown again.
            </p>
            <button onClick={() => setNewKey(null)} className="text-emerald-700 hover:text-emerald-900 text-sm">Dismiss</button>
          </div>
          <div className="flex items-center gap-3">
            <code className="flex-1 px-3 py-2 bg-white dark:bg-slate-900 border rounded font-mono text-sm break-all">
              {newKey.apiKey}
            </code>
            <button
              onClick={copyKey}
              className="px-4 py-2 bg-emerald-600 text-white text-sm font-medium rounded-lg hover:bg-emerald-700 transition whitespace-nowrap"
            >
              {copied ? 'Copied ✓' : 'Copy'}
            </button>
          </div>
        </div>
      )}

      <div className="bg-white dark:bg-slate-900 border rounded-xl overflow-hidden shadow-sm">
        <table className="w-full text-left text-sm text-slate-600 dark:text-slate-400">
          <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-700 dark:text-slate-300 font-semibold border-b">
            <tr>
              <th className="px-6 py-4">User</th>
              <th className="px-6 py-4">Provider</th>
              <th className="px-6 py-4">Masked Key</th>
              <th className="px-6 py-4">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {loading ? (
              <tr><td colSpan={4} className="px-6 py-4 text-center">Loading keys...</td></tr>
            ) : keys.length === 0 ? (
              <tr><td colSpan={4} className="px-6 py-4 text-center text-slate-400">No keys yet.</td></tr>
            ) : keys.map((key) => (
              <tr key={key.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/50">
                <td className="px-6 py-4 font-medium text-slate-900 dark:text-slate-100">{key.userName} ({key.userEmail})</td>
                <td className="px-6 py-4">
                  <span className={`px-2 py-0.5 text-xs font-semibold rounded ${key.provider === 'ANTHROPIC' ? 'bg-amber-100 text-amber-800' : 'bg-blue-100 text-blue-800'}`}>
                    {key.provider}
                  </span>
                </td>
                <td className="px-6 py-4 font-mono">{key.maskedKey}</td>
                <td className="px-6 py-4">
                  <span className="px-2 py-0.5 text-xs font-semibold rounded bg-emerald-100 text-emerald-800">
                    Active
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
