import type { SmsMessage } from "./types.js";

const messages: SmsMessage[] = [];
const MAX_MESSAGES = 500;

export function addMessage(message: SmsMessage) {
  const index = messages.findIndex(m => m.id === message.id);
  if (index >= 0) messages[index] = message;
  else messages.unshift(message);
  if (messages.length > MAX_MESSAGES) messages.length = MAX_MESSAGES;
}

export function listMessages(number?: string) {
  return number ? messages.filter(m => m.number === number) : [...messages];
}

export function clearMessages() {
  messages.length = 0;
}
