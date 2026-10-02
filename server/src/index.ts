import "dotenv/config";
import express from "express";
import cors from "cors";
import helmet from "helmet";
import crypto from "node:crypto";
import { extractOtp } from "./otp.js";
import { addMessage, listMessages } from "./store.js";
import type { ProviderWebhookPayload } from "./types.js";

const app = express();
app.use(helmet());
app.use(cors());
app.use(express.json({ limit: "64kb" }));

const PORT = Number(process.env.PORT ?? 8080);
const APP_TOKEN = process.env.APP_TOKEN ?? "";
const WEBHOOK_SECRET = process.env.WEBHOOK_SECRET ?? "";

function requireAppToken(req: express.Request, res: express.Response, next: express.NextFunction) {
  if (!APP_TOKEN || req.header("authorization") !== `Bearer ${APP_TOKEN}`) {
    return res.status(401).json({error:"unauthorized"});
  }
  next();
}

function validSignature(raw: string, signature: string | undefined) {
  if (!WEBHOOK_SECRET || !signature) return false;
  const expected = crypto.createHmac("sha256", WEBHOOK_SECRET).update(raw).digest("hex");
  return crypto.timingSafeEqual(Buffer.from(expected), Buffer.from(signature));
}

app.get("/health", (_req,res) => res.json({ok:true, service:"tempsms-receiver"}));

app.get("/api/messages", requireAppToken, (req,res) => {
  const number = typeof req.query.number === "string" ? req.query.number : undefined;
  res.json(listMessages(number));
});

app.post("/webhooks/provider", express.text({type:"application/json"}), (req,res) => {
  const raw = typeof req.body === "string" ? req.body : JSON.stringify(req.body ?? {});
  if (!validSignature(raw, req.header("x-webhook-signature"))) {
    return res.status(401).json({error:"invalid signature"});
  }
  let payload: ProviderWebhookPayload;
  try { payload = JSON.parse(raw) as ProviderWebhookPayload; }
  catch { return res.status(400).json({error:"invalid json"}); }
  if (!payload.id || !payload.number || !payload.text || !payload.receivedAt) {
    return res.status(400).json({error:"missing required fields"});
  }
  addMessage({...payload, otp: extractOtp(payload.text)});
  res.status(202).json({accepted:true});
});

app.listen(PORT, () => console.log(`TempSMS Receiver API listening on :${PORT}`));
