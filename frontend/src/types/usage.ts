import { AiProvider } from './apiKey';

export interface DailyUserUsage {
  id: string;
  userId: string;
  teamId: string;
  date: string;
  provider: AiProvider;
  model: string;
  promptTokens: number;
  completionTokens: number;
  totalTokens: number;
  estimatedCost: number;
}

export interface AIUsageTelemetryRequest {
  userId: string;
  teamId: string;
  provider: AiProvider;
  model: string;
  promptTokens: number;
  completionTokens: number;
}

export interface BudgetStatusResponse {
  teamId: string;
  monthlyBudget: number;
  currentSpend: number;
  usagePercentage: number;
  exceeded: boolean;
  alertTriggered: boolean;
}
