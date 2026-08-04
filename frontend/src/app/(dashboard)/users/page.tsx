'use client';

import { useEffect, useState } from 'react';
import { getUsers, createUser, getTeams, UserData, TeamData } from '@/lib/api-services';

export default function UsersPage() {
  const [users, setUsers] = useState<UserData[]>([]);
  const [teams, setTeams] = useState<TeamData[]>([]);
  const [loading, setLoading] = useState(true);

  const [email, setEmail] = useState('');
  const [fullName, setFullName] = useState('');
  const [role, setRole] = useState('MEMBER');
  const [teamId, setTeamId] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const load = () => {
    setLoading(true);
    Promise.all([getUsers(), getTeams()])
      .then(([u, t]) => {
        setUsers(u);
        setTeams(t);
      })
      .catch(() => {
        setUsers([]);
        setTeams([]);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    if (!email || !teamId) {
      setError('Email and team are required.');
      return;
    }
    setSubmitting(true);
    try {
      await createUser({ email, fullName, role, teamId });
      setEmail('');
      setFullName('');
      setRole('MEMBER');
      setTeamId('');
      load();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create user');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="p-8 max-w-7xl mx-auto space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-slate-900 dark:text-slate-100">Users Directory</h1>
        <p className="text-slate-500 mt-1">Register users, map them to teams, and provision AI proxy access.</p>
      </div>

      <form onSubmit={handleCreate} className="p-6 bg-white dark:bg-slate-900 border rounded-xl shadow-sm space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="space-y-1">
            <label className="text-sm font-medium text-slate-700 dark:text-slate-300">Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="dev@acme.com"
              className="w-full px-3 py-2 border rounded-lg dark:bg-slate-800 dark:border-slate-700"
              required
            />
          </div>
          <div className="space-y-1">
            <label className="text-sm font-medium text-slate-700 dark:text-slate-300">Full Name</label>
            <input
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="Dev One"
              className="w-full px-3 py-2 border rounded-lg dark:bg-slate-800 dark:border-slate-700"
            />
          </div>
          <div className="space-y-1">
            <label className="text-sm font-medium text-slate-700 dark:text-slate-300">Role</label>
            <select
              value={role}
              onChange={(e) => setRole(e.target.value)}
              className="w-full px-3 py-2 border rounded-lg dark:bg-slate-800 dark:border-slate-700"
            >
              <option value="MEMBER">MEMBER</option>
              <option value="ADMIN">ADMIN</option>
              <option value="OWNER">OWNER</option>
            </select>
          </div>
          <div className="space-y-1">
            <label className="text-sm font-medium text-slate-700 dark:text-slate-300">Team</label>
            <select
              value={teamId}
              onChange={(e) => setTeamId(e.target.value)}
              className="w-full px-3 py-2 border rounded-lg dark:bg-slate-800 dark:border-slate-700"
              required
            >
              <option value="">Select a team…</option>
              {teams.map((t) => (
                <option key={t.id} value={t.id}>{t.teamName}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <button
            type="submit"
            disabled={submitting}
            className="px-5 py-2 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 transition disabled:opacity-50"
          >
            {submitting ? 'Creating…' : 'Add User'}
          </button>
          {teams.length === 0 && !loading && (
            <span className="text-sm text-amber-600">Create a team first before adding users.</span>
          )}
          {error && <span className="text-sm text-red-600">{error}</span>}
        </div>
      </form>

      <div className="bg-white dark:bg-slate-900 border rounded-xl overflow-hidden shadow-sm">
        <table className="w-full text-left text-sm text-slate-600 dark:text-slate-400">
          <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-700 dark:text-slate-300 font-semibold border-b">
            <tr>
              <th className="px-6 py-4">Full Name</th>
              <th className="px-6 py-4">Email</th>
              <th className="px-6 py-4">Team</th>
              <th className="px-6 py-4">Role</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {loading ? (
              <tr><td colSpan={4} className="px-6 py-4 text-center">Loading users...</td></tr>
            ) : users.length === 0 ? (
              <tr><td colSpan={4} className="px-6 py-4 text-center text-slate-400">No users yet.</td></tr>
            ) : users.map((user) => (
              <tr key={user.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/50">
                <td className="px-6 py-4 font-medium text-slate-900 dark:text-slate-100">{user.fullName || 'N/A'}</td>
                <td className="px-6 py-4">{user.email}</td>
                <td className="px-6 py-4">{user.teamName || 'Unassigned'}</td>
                <td className="px-6 py-4">
                  <span className="px-2 py-0.5 text-xs font-medium rounded bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300">
                    {user.role}
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
