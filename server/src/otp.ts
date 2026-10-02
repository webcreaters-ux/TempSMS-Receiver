export function extractOtp(text: string): string | undefined {
  const patterns = [
    /\b(\d{8})\b/,
    /\b(\d{6})\b/,
    /\b(\d{5})\b/,
    /\b(\d{4})\b/
  ];
  for (const pattern of patterns) {
    const match = text.match(pattern);
    if (match?.[1]) return match[1];
  }
  return undefined;
}
