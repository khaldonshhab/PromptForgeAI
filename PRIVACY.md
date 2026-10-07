# PromptForge AI — Privacy Policy

**Last updated:** 7 October 2026

PromptForge AI is an Android application for creating and improving prompts for AI tools. This policy explains what data the app stores or transmits and how it is handled.

## 1. Data stored on the device

PromptForge AI may store the following locally on the user's device:
- App language preference.
- Generated prompt history.
- Prompts the user explicitly saves.
- Premium entitlement state used by the app.
- An optional backend URL entered by the user.

This local data is not intentionally uploaded by PromptForge AI unless the user uses a feature that sends text to a configured backend.

## 2. Prompt data sent to a backend

The app can work with its local prompt engine. If the user configures a backend URL and requests remote generation, the text entered for that generation request is transmitted to the configured server.

The server may then send the prompt content to the AI provider configured by the server operator. The PromptForge AI Android client does not contain or expose an AI-provider API key.

PromptForge AI is designed so that remote generation is user-initiated. Users should not enter passwords, payment card numbers, authentication secrets, or other highly sensitive information into prompts unless they understand the risks and have a legitimate reason to do so.

Production backend deployments should use HTTPS. Server infrastructure, hosting providers, and AI providers may maintain technical logs according to their own policies and configurations.

## 3. Payments and subscriptions

Premium subscriptions are processed through Google Play Billing. PromptForge AI does not receive or store the user's payment card number.

Google Play provides the app with purchase/subscription state needed to enable Premium features. Google may process payment and transaction information under Google's own terms and privacy policy.

## 4. Third-party services

Depending on how the app is deployed and configured, data may be processed by:
- Google Play / Google Play Billing for app distribution and subscriptions.
- The backend server configured by the user or operator.
- The AI provider configured by that backend.

Each third-party service may have its own privacy policy and data-retention rules.

## 5. Data retention and deletion

Local history and saved prompts can be removed by clearing the app's data or uninstalling the app.

PromptForge AI's Android client does not create a user account. Therefore there is no account database in the Android client to delete.

If a backend stores request data, deletion and retention are controlled by that backend's deployment and logging configuration. A production operator should configure the backend to minimize retention and provide a deletion/contact mechanism appropriate to the service.

## 6. Security

Remote requests should be made only over HTTPS. API keys for AI providers must remain on the server and must not be embedded in the Android application.

## 7. Children's privacy

PromptForge AI is not specifically directed to children. The app should not be used to collect personal information from children.

## 8. Contact

For privacy questions or requests concerning the PromptForge AI project, contact the project owner through the public project repository:

https://github.com/khaldonshhab/PromptForgeAI

## 9. Changes to this policy

This policy may be updated when the app's data practices, backend architecture, or third-party services change. The latest version will be published with the project.

---

# سياسة الخصوصية — PromptForge AI

**آخر تحديث: 7 تشرين الأول 2026**

PromptForge AI هو تطبيق أندرويد لإنشاء وتحسين أوامر الذكاء الاصطناعي (Prompts).

### البيانات المحفوظة على الجهاز
قد يحفظ التطبيق محلياً:
- لغة التطبيق.
- سجل البرومبتات الناتجة.
- البرومبتات التي يختار المستخدم حفظها.
- حالة Premium المستخدمة داخل التطبيق.
- رابط الخادم الاختياري الذي يدخله المستخدم.

### إرسال البرومبتات إلى الخادم
يمكن استخدام المحرك المحلي دون إرسال البيانات خارج الجهاز. عند ضبط رابط خادم واختيار التوليد عن بُعد، يرسل التطبيق النص الذي أدخله المستخدم لهذا الطلب إلى الخادم المحدد.

قد يرسل الخادم النص بعد ذلك إلى مزود الذكاء الاصطناعي الذي تم ضبطه على الخادم. مفاتيح مزودي الذكاء الاصطناعي لا توضع داخل تطبيق أندرويد.

يجب عدم إدخال كلمات المرور أو بيانات البطاقات أو مفاتيح الدخول أو المعلومات الحساسة جداً داخل البرومبتات إلا عند فهم المخاطر والحاجة المشروعة لذلك.

### الاشتراكات
تتم عمليات Premium عبر Google Play Billing. لا يحصل التطبيق على رقم بطاقة الدفع ولا يخزنه.

### الاحتفاظ والحذف
يتم تخزين السجل والمحفوظات محلياً، ويمكن حذفها عبر مسح بيانات التطبيق أو إلغاء تثبيته. التطبيق لا ينشئ حساب مستخدم في نسخته الحالية.

إذا كان الخادم البعيد يحتفظ بالطلبات أو السجلات، فتخضع مدة الاحتفاظ لإعدادات ذلك الخادم ومزود الاستضافة ومزود الذكاء الاصطناعي.

### الأمان
يجب استخدام HTTPS في الخادم الإنتاجي. يجب إبقاء مفاتيح مزودي الذكاء الاصطناعي على الخادم وعدم وضعها داخل التطبيق.

### التواصل
للاستفسارات المتعلقة بالخصوصية، يمكن التواصل عبر مستودع المشروع العام:
https://github.com/khaldonshhab/PromptForgeAI
