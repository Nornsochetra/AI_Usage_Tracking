import { fetchApi } from '@/lib/api-client';
import { BudgetStatusResponse } from '@/types/usage';

export async function getBudgetStatus(teamId: string): Promise<BudgetStatusResponse> {
  return fetchApi<BudgetStatusResponse>(`/api/usage/budget/${teamId}`);
}
