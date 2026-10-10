import test from 'node:test';
import assert from 'node:assert/strict';
import { buildGeminiModelUrl, buildGeminiRequest, extractGeminiText, geminiErrorMessage } from './server.mjs';

test('buildGeminiRequest uses Google GenerateContent contract', () => {
  const request = buildGeminiRequest({
    apiUrl: 'https://generativelanguage.googleapis.com/v1beta',
    apiKey: 'test-key',
    model: 'gemini-2.5-flash',
    system: 'You are helpful.',
    user: 'Write a short prompt.'
  });

  assert.equal(request.url, 'https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=test-key');
  assert.equal(request.body.systemInstruction.parts[0].text, 'You are helpful.');
  assert.equal(request.body.contents[0].parts[0].text, 'Write a short prompt.');
});

test('buildGeminiRequest preserves v1beta when URL includes /models', () => {
  const request = buildGeminiRequest({
    apiUrl: 'https://generativelanguage.googleapis.com/v1beta/models',
    apiKey: 'test-key',
    model: 'gemini-2.5-flash',
    system: 'System',
    user: 'User'
  });
  assert.equal(request.url, 'https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=test-key');
});

test('buildGeminiRequest adds v1beta when given the API root', () => {
  const request = buildGeminiRequest({
    apiUrl: 'https://generativelanguage.googleapis.com',
    apiKey: 'test-key',
    model: 'gemini-2.5-flash',
    system: 'System',
    user: 'User'
  });
  assert.equal(request.url, 'https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=test-key');
});

test('buildGeminiModelUrl checks the configured Gemini model', () => {
  assert.equal(
    buildGeminiModelUrl({
      apiUrl: 'https://generativelanguage.googleapis.com/v1beta',
      apiKey: 'test-key',
      model: 'gemini-2.5-flash'
    }),
    'https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash?key=test-key'
  );
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

test('buildGeminiRequest sends Arabic input directly with English prompt instructions', () => {
  const request = buildGeminiRequest({
    apiUrl: 'https://generativelanguage.googleapis.com/v1beta',
    apiKey: 'test-key',
    model: 'gemini-3.8-flash',
    system: 'Understand the original language and return only a professional prompt in English.',
    user: 'أنشئ صورة سينمائية في دمشق ليلاً'
  });

  assert.equal(request.body.systemInstruction.parts[0].text.includes('in English'), true);
  assert.equal(request.body.contents[0].parts[0].text, 'أنشئ صورة سينمائية في دمشق ليلاً');
  assert.match(request.url, /models\/gemini-3\.8-flash:generateContent/);
});

test('Gemini model errors identify the required config without echoing provider details', () => {
  const message = geminiErrorMessage(JSON.stringify({
    error: { message: 'This model is no longer available to new users. Internal diagnostic: secret details.' }
  }), 500, 'gemini-2.5-flash');

  assert.match(message, /AI_MODEL/);
  assert.doesNotMatch(message, /secret details|no longer available to new users/);
});

test('invalid Gemini API keys produce a clear safe error', () => {
  const message = geminiErrorMessage(JSON.stringify({
    error: { status: 'API_KEY_INVALID', message: 'API key not valid. Raw upstream detail.' }
  }), 400, 'gemini-3.8-flash');

  assert.match(message, /AI_API_KEY/);
  assert.doesNotMatch(message, /Raw upstream detail/);
});
