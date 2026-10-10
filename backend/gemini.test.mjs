import test from 'node:test';
import assert from 'node:assert/strict';
import { buildGeminiRequest, extractGeminiText } from './server.mjs';

test('buildGeminiRequest uses Google GenerateContent contract', () => {
  const request = buildGeminiRequest({
    apiUrl: 'https://generativelanguage.googleapis.com/v1beta/models',
    apiKey: 'test-key',
    model: 'gemini-2.5-flash',
    system: 'You are helpful.',
    user: 'Write a short prompt.'
  });

  assert.equal(request.url, 'https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=test-key');
  assert.equal(request.body.systemInstruction.parts[0].text, 'You are helpful.');
  assert.equal(request.body.contents[0].parts[0].text, 'Write a short prompt.');
});

test('extractGeminiText reads the Google response format', () => {
  const text = extractGeminiText({
    candidates: [{
      content: {
        parts: [{ text: 'Final prompt text' }]
      }
    }]
  });

  assert.equal(text, 'Final prompt text');
});
