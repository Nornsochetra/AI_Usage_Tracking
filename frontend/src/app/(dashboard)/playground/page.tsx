'use client';

import { useEffect, useState } from 'react';
import { getUsers, runPlayground, UserData, PlaygroundResult } from '@/lib/api-services';
import { formatCurrency, formatNumber } from '@/lib/utils';

const DEFAULT_MODEL: Record<string, string> = {
  GEMINI: 'gemini-flash-latest',
  ANTHROPIC: 'claude-3-5-haiku-latest',
};

export default function PlaygroundPage() {
  const [users, setUsers] = useState<UserData[]>([]);
  const [userId, setUserId] = useState('');
  const [provider, setProvider] = useState('GEMINI');
  const [model, setModel] = useState(DEFAULT_MODEL.GEMINI);
  const [prompt, setPrompt] = useState('Reply with exactly one word: hello');
  const [running, setRunning] = useState(false);
  const [error, setError] = useState('');
  const [result, setResult] = useState<PlaygroundResult | null>(null);

  useEffect(() => {
    getUsers().then(setUsers).catch(() => setUsers([]));
  }, []);

  const onProviderChange = (p: string) => {
    setProvider(p);
    setModel(DEFAULT_MODEL[p]);
  };

  const handleRun = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setResult(null);
    if (!userId) {
      setError('Select a user (usage is recorded against their team).');
      return;
    }
    setRunning(true);
    try {
      const res = await runPlayground({ userId, provider, model, prompt });
      setResult(res);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Request failed');
    } finally {
      setRunning(false);
    }
  };

  return (
    <div className="p-8 max-w-5xl mx-auto space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-slate-900 dark:text-slate-100">Playground</h1>
        <p className="text-slate-500 mt-1">
          Send a real prompt through the proxy and watch the tokens &amp; cost get recorded — no CLI needed.
        </p>
      </div>

      <form onSubmit={handleRun} className="p-6 bg-white dark:bg-slate-900 border rounded-xl shadow-sm space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="space-y-1">
            <label className="text-sm font-medium text-slate-700 dark:text-slate-300">User (billed team)</label>
            <select
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
              className="w-full px-3 py-2 border rounded-lg dark:bg-slate-800 dark:border-slate-700"
              required
            >
              <option value="">Select a user…</option>
              {users.map((u) => (
                <option key={u.id} value={u.id}>{u.email}{u.teamName ? ` — ${u.teamName}` : ''}</option>
              ))}
            </select>
          </div>
          <div className="space-y-1">
            <label className="text-sm font-medium text-slate-700 dark:text-slate-300">Provider</label>
            <select
              value={provider}
              onChange={(e) => onProviderChange(e.target.value)}
              className="w-full px-3 py-2 border rounded-lg dark:bg-slate-800 dark:border-slate-700"
            >
              <option value="GEMINI">GEMINI</option>
              <option value="ANTHROPIC">ANTHROPIC</option>
            </select>
          </div>
          <div className="space-y-1">
            <label className="text-sm font-medium text-slate-700 dark:text-slate-300">Model</label>
            <input
              type="text"
              value={model}
              onChange={(e) => setModel(e.target.value)}
              className="w-full px-3 py-2 border rounded-lg dark:bg-slate-800 dark:border-slate-700 font-mono text-sm"
            />
          </div>
        </div>

        <div className="space-y-1">
          <label className="text-sm font-medium text-slate-700 dark:text-slate-300">Prompt</label>
          <textarea
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
            rows={4}
            className="w-full px-3 py-2 border rounded-lg dark:bg-slate-800 dark:border-slate-700"
            required
          />
        </div>

        <div className="flex items-center gap-4">
          <button
            type="submit"
            disabled={running}
            className="px-5 py-2 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 transition disabled:opacity-50"
          >
            {running ? 'Sending…' : 'Send through proxy'}
          </button>
          {users.length === 0 && (
            <span className="text-sm text-amber-600">Create a user first.</span>
          )}
          {error && <span className="text-sm text-red-600">{error}</span>}
        </div>
      </form>

      {result && (
        result.success ? (
          <div className="space-y-4">
            <div className="flex flex-wrap gap-3">
              <Chip label="Model" value={result.model} mono />
              <Chip label="Prompt tokens" value={formatNumber(result.promptTokens || 0)} />
              <Chip label="Completion tokens" value={formatNumber(result.completionTokens || 0)} />
              <Chip label="Cost" value={formatCurrency(result.costUsd || 0)} />
            </div>
            <div className="p-5 bg-white dark:bg-slate-900 border rounded-xl shadow-sm">
              <h2 className="text-sm font-semibold text-slate-500 mb-2">Response</h2>
              <p className="whitespace-pre-wrap text-slate-900 dark:text-slate-100">{result.responseText}</p>
            </div>
            <p className="text-sm text-emerald-600">
              ✓ Recorded — refresh the Dashboard to see the totals update.
            </p>
          </div>
        ) : (
          <div className="p-5 bg-red-50 dark:bg-red-950/40 border border-red-300 dark:border-red-800 rounded-xl">
            <h2 className="text-sm font-semibold text-red-700 dark:text-red-300 mb-1">Provider error</h2>
            <p className="text-sm text-red-600 dark:text-red-400 break-words">{result.error}</p>
          </div>
        )
      )}
    </div>
  );
}

function Chip({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <div className="px-4 py-2 bg-slate-100 dark:bg-slate-800 rounded-lg">
      <div className="text-xs text-slate-500">{label}</div>
      <div className={`text-sm font-semibold text-slate-900 dark:text-slate-100 ${mono ? 'font-mono' : ''}`}>{value}</div>
    </div>
  );
}
