export interface AuthorizedSmsProvider {
  listNumbers(): Promise<Array<{id:string; number:string}>>;
  listMessages(numberId:string): Promise<unknown[]>;
  releaseNumber?(numberId:string): Promise<void>;
}

// Implement this interface for the provider you legitimately subscribe to.
// Keep provider credentials server-side and never ship them in the Android APK.
