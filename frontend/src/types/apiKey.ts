export type AiProvider = 'ANTHROPIC' | 'GEMINI';

export interface ApiKey {
  id: string;
  keyName: string;
  provider: AiProvider;
  maskedKey: string;
  teamId?: string;
  active: boolean;
  createdAt: string;
}
