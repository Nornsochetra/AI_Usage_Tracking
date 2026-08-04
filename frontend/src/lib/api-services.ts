import { fetchApi } from '@/lib/api-client';

export interface DashboardStats {
  totalTeams: number;
  totalUsers: number;
  totalApiKeys: number;
  totalPromptTokens: number;
  totalCompletionTokens: number;
  totalSpendUsd: number;
}

export interface TeamData {
  id: string;
  teamName: string;
  monthlyBudgetUsd: number;
  currentSpendUsd: number;
  memberCount: number;
  createdAt: string;
}

export interface UserData {
  id: string;
  email: string;
  fullName: string;
  role: 'OWNER' | 'ADMIN' | 'MEMBER';
  teamId?: string;
  teamName?: string;
  createdAt: string;
}

export interface ApiKeyData {
  id: string;
  userEmail: string;
  userName: string;
  provider: 'ANTHROPIC' | 'GEMINI';
  maskedKey: string;
  createdAt: string;
}

export async function getDashboardStats(): Promise<DashboardStats> {
  return fetchApi<DashboardStats>('/api/analytics/dashboard');
}

export async function getTeams(): Promise<TeamData[]> {
  return fetchApi<TeamData[]>('/api/teams');
}

export async function createTeam(data: { teamName: string; monthlyBudgetUsd: number }): Promise<TeamData> {
  return fetchApi<TeamData>('/api/teams', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function getUsers(): Promise<UserData[]> {
  return fetchApi<UserData[]>('/api/users');
}

export async function createUser(data: {
  email: string;
  fullName: string;
  role: string;
  teamId: string;
}): Promise<UserData> {
  return fetchApi<UserData>('/api/users', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function getApiKeys(): Promise<ApiKeyData[]> {
  return fetchApi<ApiKeyData[]>('/api/api-keys');
}

// Returned only at creation time — contains the full plaintext key, shown once.
export interface CreatedApiKey {
  id: string;
  userEmail: string;
  provider: 'ANTHROPIC' | 'GEMINI';
  apiKey: string;
  createdAt: string;
}

export async function createApiKey(data: {
  userId: string;
  provider: string;
}): Promise<CreatedApiKey> {
  return fetchApi<CreatedApiKey>('/api/api-keys', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export interface PlaygroundResult {
  success: boolean;
  model: string;
  responseText?: string;
  promptTokens?: number;
  completionTokens?: number;
  costUsd?: number;
  error?: string;
}

export async function runPlayground(data: {
  userId: string;
  provider: string;
  model?: string;
  prompt: string;
}): Promise<PlaygroundResult> {
  return fetchApi<PlaygroundResult>('/api/playground/generate', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}
