package com.promptforge.ai;

public final class PromptEngine {
    private PromptEngine() {}

    public static String generate(String idea, Platform p, String task, boolean ar) {
        String x = idea == null || idea.trim().isEmpty()
                ? (ar ? "أنشئ نتيجة احترافية لمشروعي" : "Create a professional result for my project")
                : idea.trim();
        String n = p == null ? "ChatGPT" : p.name;
        String style = p == null ? "general" : p.style;
        String t = task == null ? "General" : task;

        if ("image".equals(style)) return image(x,n,t,ar);
        if ("video".equals(style)) return video(x,n,t,ar);
        if ("voice".equals(style)) return voice(x,n,t,ar);
        if ("music".equals(style)) return music(x,n,t,ar);
        if ("coding".equals(style)) return coding(x,n,t,ar);
        if ("research".equals(style)) return research(x,n,t,ar);
        if ("marketing".equals(style)) return marketing(x,n,t,ar);
        if ("writing".equals(style)) return writing(x,n,t,ar);
        if ("presentations".equals(style)) return presentation(x,n,t,ar);
        if ("design".equals(style)) return design(x,n,t,ar);
        if ("3d".equals(style)) return threeD(x,n,t,ar);
        return chat(x,n,t,ar);
    }

    private static String chat(String x,String n,String t,boolean ar){
        if(ar) return "أنت تستخدم "+n+" كمساعد متخصص.\n\n"+
                "المهمة: "+t+"\nالفكرة الأصلية: "+x+"\n\n"+
                "حلّل الطلب قبل الإجابة. حدّد الافتراضات الناقصة فقط عندما تكون مؤثرة. "+
                "قدّم إجابة مباشرة ومنظمة، واستخدم عناوين وقوائم عند الحاجة. لا تكرر الطلب ولا تذكر أنك نموذج ذكاء اصطناعي. "+
                "إذا كانت المهمة تتطلب خطوات أو كوداً أو قراراً، اجعل الناتج قابلاً للتنفيذ مع ذكر القيود المهمة.";
        return "Act as a specialized assistant inside "+n+".\n\nTask: "+t+
                "\nOriginal idea: "+x+"\n\n"+
                "Understand the intent before answering. Ask only for missing information that materially changes the result. "+
                "Return a direct, structured, actionable answer. Use headings, lists, examples or code when useful. "+
                "Do not restate the request or add generic AI disclaimers. State important assumptions and constraints.";
    }

    private static String research(String x,String n,String t,boolean ar){
        if(ar) return "أنت باحث متخصص يعمل داخل "+n+".\n\n"+
                "سؤال البحث: "+x+"\nنوع المهمة: "+t+"\n\n"+
                "ابنِ الإجابة على الأدلة والمصادر القابلة للتحقق. افصل بين الحقائق والاستنتاجات والآراء. "+
                "اذكر تاريخ المعلومات عندما تكون حساسة للزمن، وحدد ما يحتاج إلى تحقق إضافي. "+
                "نظّم الناتج إلى: خلاصة، أدلة رئيسية، مصادر/روابط إن كانت المنصة تدعمها، ثم نقاط عدم اليقين.";
        return "You are a rigorous research assistant working in "+n+".\n\nResearch question: "+x+
                "\nTask: "+t+"\n\n"+
                "Use evidence and verifiable sources. Separate facts, inferences and opinions. "+
                "Date-stamp time-sensitive claims, flag uncertainty, and never invent citations. "+
                "Structure the result as: executive summary, key evidence, sources/links when supported, and open questions.";
    }

    private static String image(String x,String n,String t,boolean ar){
        if ("Midjourney".equals(n)) return (ar?"أنشئ صورة لـ: ":"Create an image of: ")+x+
                "\n"+(ar?"حوّلها إلى برومبت Midjourney محدد بصرياً يتضمن الموضوع، البيئة، التكوين، منظور الكاميرا/العدسة عند الحاجة، الإضاءة، الخامة، المزاج والأسلوب. استخدم معاملات Midjourney فقط عندما تخدم الفكرة، مثل --ar أو --stylize، ولا تضفها عشوائياً. لا تكتب شرحاً.":"Turn this into a Midjourney-native visual prompt covering subject, environment, composition, camera/lens when useful, lighting, material, mood and style. Use Midjourney parameters such as --ar or --stylize only when justified. Return the prompt only.");
        if ("Stable Diffusion".equals(n)) return (ar?"المشهد: ":"Scene: ")+x+
                "\n"+(ar?"اكتب Prompt لـ Stable Diffusion/SDXL بطبقتين: positive prompt غني بالتفاصيل البصرية، ثم Negative Prompt مستقل لتقليل التشوهات والعيوب. استخدم أوزاناً مثل (term:1.2) فقط عندما تكون مفيدة، ولا تعتمد على معاملات واجهة غير مؤكدة.":"Write a Stable Diffusion/SDXL prompt with two explicit parts: a detailed positive prompt and a separate Negative Prompt for common defects. Use weights such as (term:1.2) only when useful; avoid unsupported UI parameters.");
        if ("Ideogram".equals(n)) return (ar?"حوّل الفكرة إلى برومبت Ideogram: ":"Convert the idea into an Ideogram prompt: ")+x+
                (ar?". ركّز على التكوين والرسالة البصرية والنص الظاهر داخل الصورة إن وُجد، وحدد النص حرفياً بين علامات واضحة.":" Focus on composition, visual message, and any text that must appear in the image; specify required text exactly.");
        if ("Recraft".equals(n) || "Canva AI".equals(n) || "Canva Magic Studio".equals(n))
            return (ar?"أنت مصمم بصري. أنشئ برومبت مخصص لـ "+n+" للفكرة التالية: ":"You are a visual designer. Create a "+n+"-optimized prompt for: ")+x+
                    (ar?". حدّد نوع التصميم، الشبكة، التسلسل البصري، الألوان، الخطوط، العناصر، الاستخدام النهائي، ونسبة الأبعاد عند الحاجة.":" Specify design type, layout/grid, visual hierarchy, palette, typography, elements, end use, and aspect ratio when relevant.");
        return (ar?"أنشئ برومبت صورة متخصصاً لـ "+n+" للفكرة التالية: ":"Create a platform-specific image prompt for "+n+" for this idea: ")+x+
                (ar?". اذكر الموضوع، البيئة، التكوين، الإضاءة، الألوان، الخامات، العدسة/المنظور عند الحاجة، الأسلوب والمزاج. لا تضف معاملات خاصة بمنصة أخرى.":" Include subject, environment, composition, lighting, palette, materials, camera/perspective when useful, style and mood. Do not import syntax from another platform.");
    }

    private static String video(String x,String n,String t,boolean ar){
        String rule = "Runway".equals(n) ? "motion-first language, shot type, camera movement, subject motion, timing and continuity"
                : "Google Veo".equals(n) ? "cinematic shot description, camera movement, temporal progression, lighting and realistic physical motion"
                : "Kling AI".equals(n) ? "subject consistency, camera path, motion intensity, timing and scene continuity"
                : "Pika".equals(n) ? "clear subject action, transformation/effect, camera behavior and concise timing"
                : "Luma Dream Machine".equals(n) ? "cinematic composition, camera motion, environment and natural movement"
                : "Hailuo AI".equals(n) ? "strong action description, character motion, camera movement and temporal order"
                : "Vidu".equals(n) ? "character consistency, shot composition and controlled motion"
                : "LTX Studio".equals(n) ? "shot planning, characters, locations, camera directions and continuity"
                : "temporal action, camera behavior, subject consistency and physical motion";
        if(ar) return "أنشئ برومبت فيديو أصلي لـ "+n+".\n\nالفكرة: "+x+"\nنوع المهمة: "+t+
                "\n\nمنهج المنصة: "+rule+".\nاكتب المشهد بترتيب زمني واضح: اللقطة، حركة الكاميرا، حركة الشخص/العنصر، البيئة والإضاءة، ثم الانتقال أو النهاية. "+
                "تجنب حشد أحداث متزامنة غير قابلة للتنفيذ، ولا تستخدم معاملات خاصة بمنصة أخرى.";
        return "Create a native video-generation prompt for "+n+".\n\nIdea: "+x+"\nTask: "+t+
                "\n\nPlatform emphasis: "+rule+".\nDescribe the shot in temporal order: framing, camera movement, subject/environment motion, lighting, then transition or ending. "+
                "Avoid overloaded simultaneous actions and do not use syntax from another platform.";
    }

    private static String voice(String x,String n,String t,boolean ar){
        String cues = "ElevenLabs".equals(n) ? "delivery, emotion, pacing, emphasis and pronunciation cues without pretending unsupported markup"
                : "voice identity, tone, pace, emotion, pronunciation and delivery";
        return (ar?"أنشئ Prompt مخصصاً لـ "+n+" للصوت/تحويل النص إلى كلام.\n\nالنص أو الفكرة: ":"Create a "+n+"-optimized voice/TTS prompt.\n\nText or idea: ")+x+
                "\n"+(ar?"نوع المهمة: ":"Task: ")+t+"\n\n"+
                (ar?"حدّد الشخصية الصوتية، العمر التقريبي، النبرة، السرعة، الإيقاع، العاطفة، مخارج الكلمات والنطق الصحيح. استخدم تعليمات أداء قابلة للتنفيذ، ولا تخلط صيغ منصات أخرى.":"Define voice identity, approximate age, tone, pace, rhythm, emotion, pronunciation and delivery. Use actionable performance direction and avoid syntax from other platforms.")+
                "\n"+cues+".";
    }

    private static String music(String x,String n,String t,boolean ar){
        return (ar?"أنشئ برومبت موسيقى أصلياً لـ "+n+".\n\nالفكرة: ":"Create a native music-generation prompt for "+n+".\n\nIdea: ")+x+
                "\n"+(ar?"حدّد النوع، المزاج، السرعة التقريبية، الآلات، بنية الأغنية، طبيعة الصوت/الغناء، الإنتاج والمرجع الزمني أو الثقافي إن كان مهماً. إذا كانت هناك كلمات، افصل الكلمات عن وصف الأسلوب.":"Specify genre, mood, approximate tempo, instrumentation, song structure, vocal character, production and relevant era/cultural reference. If lyrics are needed, separate lyrics from style direction.")+
                "\n"+(ar?"نوع المهمة: ":"Task: ")+t;
    }

    private static String coding(String x,String n,String t,boolean ar){
        return (ar?"أنت مهندس برمجيات يعمل داخل "+n+".\n\nالمطلوب: ":"You are a senior software engineer working inside "+n+".\n\nRequest: ")+x+
                "\n"+(ar?"نوع المهمة: ":"Task: ")+t+"\n\n"+
                (ar?"قبل التنفيذ: استخرج المتطلبات والافتراضات والقيود. افحص بنية المشروع قبل تغييرها، حافظ على التوافق، واذكر الملفات المتأثرة. عند كتابة الكود قدّم حلاً قابلاً للتشغيل مع معالجة الأخطاء واختبارات مناسبة. لا تخترع APIs أو ملفات غير موجودة.":"Before implementation, extract requirements, assumptions and constraints. Inspect the existing project structure before changing it, preserve compatibility, and identify affected files. Provide runnable code with error handling and appropriate tests. Never invent APIs or files.")+
                "\n"+(ar?"أسلوب "+n+": اجعل الأوامر والتعديلات قابلة للتطبيق مباشرة داخل بيئة التطوير، وتجنب الحشو.":"Environment: "+n+". Make edits and commands directly actionable inside the development environment; avoid filler.");
    }

    private static String marketing(String x,String n,String t,boolean ar){
        return (ar?"أنشئ برومبت تسويق متخصصاً لـ "+n+".\n\nالمنتج/الفكرة: ":"Create a marketing-native prompt for "+n+".\n\nProduct/idea: ")+x+
                "\n"+(ar?"المهمة: ":"Task: ")+t+"\n\n"+
                (ar?"حدّد الجمهور، العرض، مرحلة الوعي، الرسالة الأساسية، النبرة، القناة، الدعوة إلى الإجراء، والقيود. اطلب مخرجات قابلة للقياس مع بدائل عند الحاجة.":"Specify audience, offer, awareness stage, core message, tone, channel, CTA and constraints. Request measurable outputs and variants when useful.");
    }

    private static String writing(String x,String n,String t,boolean ar){
        return (ar?"اكتب برومبت تحرير متخصصاً لـ "+n+".\n\nالمحتوى: ":"Create a writing/editing prompt optimized for "+n+".\n\nContent: ")+x+
                "\n"+(ar?"المهمة: ":"Task: ")+t+"\n\n"+
                (ar?"حافظ على المعنى والصوت ما لم يُطلب تغييره، وحدد الجمهور والطول والبنية والنبرة ومعايير الجودة.":"Preserve meaning and author voice unless change is requested; specify audience, length, structure, tone and quality criteria.");
    }

    private static String presentation(String x,String n,String t,boolean ar){
        return (ar?"أنشئ برومبت عرض تقديمي مخصصاً لـ "+n+".\n\nالموضوع: ":"Create a presentation-native prompt for "+n+".\n\nTopic: ")+x+
                "\n"+(ar?"المهمة: ":"Task: ")+t+"\n\n"+
                (ar?"حدّد الجمهور، الهدف، عدد الشرائح التقريبي، تسلسل القصة، الرسالة في كل شريحة، العناصر البصرية، البيانات، ونبرة العرض. لا تحوّل كل شريحة إلى فقرة نصية.":"Define audience, objective, approximate slide count, narrative sequence, one key message per slide, visuals, data and presentation tone. Do not turn slides into dense paragraphs.");
    }

    private static String design(String x,String n,String t,boolean ar){
        return (ar?"أنشئ برومبت تصميم متخصصاً لـ "+n+".\n\nالفكرة: ":"Create a design-native prompt for "+n+".\n\nIdea: ")+x+
                "\n"+(ar?"المهمة: ":"Task: ")+t+"\n\n"+
                (ar?"حدّد الاستخدام النهائي، المقاس، الشبكة، التسلسل البصري، الهوية، الألوان، الخطوط، المكونات والتباعد، مع مراعاة قابلية الاستخدام والطباعة/الشاشة عند الحاجة.":"Specify end use, dimensions, grid, visual hierarchy, identity, palette, typography, components and spacing, considering usability and print/screen constraints when relevant.");
    }

    private static String threeD(String x,String n,String t,boolean ar){
        return (ar?"أنشئ برومبت 3D مخصصاً لـ "+n+" للفكرة: ":"Create a 3D-generation prompt optimized for "+n+" for this idea: ")+x+
                "\n"+(ar?"حدّد الشكل الهندسي، النسب، الخامة، تفاصيل السطح، الإضاءة، الكاميرا، الخلفية، زاوية العرض ومتطلبات الاستخدام النهائي.":"Specify geometry, proportions, materials, surface detail, lighting, camera, background, view angle and intended use.");
    }

    public static String improve(String s,boolean ar){
        String x=s==null?"":s.trim();
        if(x.isEmpty()) x=ar?"اكتب برومبتاً احترافياً لمهمتي.":"Write a professional prompt for my task.";
        return ar
                ?"أعد هندسة البرومبت التالي ليصبح أدق وأكثر قابلية للتنفيذ. استخرج الهدف والسياق والقيود ومعايير الجودة وشكل المخرجات، واحذف التكرار والغموض. حافظ على نية الكاتب ولا تضف متطلبات غير مذكورة.\n\n"+x+"\n\nأخرج النسخة النهائية فقط."
                :"Re-engineer the following prompt for precision and reliable execution. Extract objective, context, constraints, quality criteria and output format; remove ambiguity and repetition. Preserve the author's intent and do not invent requirements.\n\n"+x+"\n\nReturn only the final prompt.";
    }
}