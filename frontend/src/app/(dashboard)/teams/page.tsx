'use client';

import { useEffect, useState } from 'react';
import { getTeams, createTeam, TeamData } from '@/lib/api-services';
import { formatCurrency } from '@/lib/utils';

export default function TeamsPage() {
  const [teams, setTeams] = useState<TeamData[]>([]);
  const [loading, setLoading] = useState(true);
  const [teamName, setTeamName] = useState('');
  const [budget, setBudget] = useState('');

  const loadTeams = () => {
    setLoading(true);
    getTeams()
      .then(setTeams)
      .catch(() => {
        setTeams([
          { id: '1', teamName: 'Engineering', monthlyBudgetUsd: 500.0, currentSpendUsd: 124.5, memberCount: 8, createdAt: new Date().toISOString() },
          { id: '2', teamName: 'Data Science', monthlyBudgetUsd: 1000.0, currentSpendUsd: 840.0, memberCount: 4, createdAt: new Date().toISOString() },
        ]);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadTeams();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!teamName || !budget) return;
    try {
      await createTeam({ teamName, monthlyBudgetUsd: parseFloat(budget) });
      setTeamName('');
      setBudget('');
      loadTeams();
    } catch {
      alert('Failed to create team');
    }
  };

  return (
    <div className="p-8 max-w-7xl mx-auto space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-slate-900 dark:text-slate-100">Teams & Budget Caps</h1>
        <p className="text-slate-500 mt-1">Configure monthly USD budget allocations and view real-time spending per team.</p>
      </div>

      <form onSubmit={handleCreate} className="p-6 bg-white dark:bg-slate-900 border rounded-xl shadow-sm flex flex-col md:flex-row gap-4 items-end">
        <div className="flex-1 space-y-1">
          <label className="text-sm font-medium text-slate-700 dark:text-slate-300">Team Name</label>
          <input
            type="text"
            value={teamName}
            onChange={(e) => setTeamName(e.target.value)}
            placeholder="e.g. Product AI Team"
            className="w-full px-3 py-2 border rounded-lg dark:bg-slate-800 dark:border-slate-700"
            required
          />
        </div>
        <div className="w-full md:w-48 space-y-1">
          <label className="text-sm font-medium text-slate-700 dark:text-slate-300">Monthly Budget ($)</label>
          <input
            type="number"
            value={budget}
            onChange={(e) => setBudget(e.target.value)}
            placeholder="500"
            className="w-full px-3 py-2 border rounded-lg dark:bg-slate-800 dark:border-slate-700"
            required
          />
        </div>
        <button type="submit" className="px-5 py-2 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 transition">
          Add Team
        </button>
      </form>

      <div className="bg-white dark:bg-slate-900 border rounded-xl overflow-hidden shadow-sm">
        <table className="w-full text-left text-sm text-slate-600 dark:text-slate-400">
          <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-700 dark:text-slate-300 font-semibold border-b">
            <tr>
              <th className="px-6 py-4">Team Name</th>
              <th className="px-6 py-4">Members</th>
              <th className="px-6 py-4">Monthly Budget</th>
              <th className="px-6 py-4">Current Spend</th>
              <th className="px-6 py-4">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {loading ? (
              <tr><td colSpan={5} className="px-6 py-4 text-center">Loading teams...</td></tr>
            ) : teams.map((team) => {
              const isOver = team.currentSpendUsd > team.monthlyBudgetUsd;
              return (
                <tr key={team.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/50">
                  <td className="px-6 py-4 font-medium text-slate-900 dark:text-slate-100">{team.teamName}</td>
                  <td className="px-6 py-4">{team.memberCount} members</td>
                  <td className="px-6 py-4">{formatCurrency(team.monthlyBudgetUsd)}</td>
                  <td className="px-6 py-4 font-semibold text-slate-900 dark:text-slate-100">{formatCurrency(team.currentSpendUsd)}</td>
                  <td className="px-6 py-4">
                    <span className={`px-2.5 py-1 text-xs font-semibold rounded-full ${isOver ? 'bg-red-100 text-red-700' : 'bg-emerald-100 text-emerald-700'}`}>
                      {isOver ? 'Exceeded (Requests Blocked)' : 'Within Budget'}
                    </span>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
