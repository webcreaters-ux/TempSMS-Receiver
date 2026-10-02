import assert from "node:assert/strict";
import { extractOtp } from "../otp.js";
assert.equal(extractOtp("Your code is 123456"), "123456");
assert.equal(extractOtp("Use 4821 to continue"), "4821");
assert.equal(extractOtp("No code here"), undefined);
console.log("OTP tests passed");
