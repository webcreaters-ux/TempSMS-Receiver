export type SmsMessage = {
  id: string;
  number: string;
  sender: string;
  text: string;
  receivedAt: string;
  otp?: string;
};

export type ProviderWebhookPayload = Omit<SmsMessage, "otp">;
