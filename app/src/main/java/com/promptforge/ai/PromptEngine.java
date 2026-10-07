package com.promptforge.ai;

public final class PromptEngine {
    private PromptEngine() {}

    public static String generate(String idea, Platform p, String task, boolean ar) {
        String x = idea == null || idea.trim().isEmpty()
                ? (ar ? "أنشئ نتيجة احترافية لمهمتي" : "Create a professional result for my task")
                : idea.trim();
        String n = p == null ? "ChatGPT" : p.name;
        String style = p == null ? "general" : p.style;
        String t = task == null ? "General" : task;

        if (is(n,"ChatGPT")) return chatgpt(x,t,ar);
        if (is(n,"Claude")) return claude(x,t,ar);
        if (is(n,"Gemini")) return gemini(x,t,ar);
        if (is(n,"Grok")) return grok(x,t,ar);
        if (is(n,"Copilot")) return copilot(x,t,ar);
        if (is(n,"DeepSeek")) return deepseek(x,t,ar);
        if (is(n,"Le Chat")) return lechat(x,t,ar);
        if (is(n,"Poe")) return poe(x,t,ar);
        if (is(n,"Meta AI")) return meta(x,t,ar);
        if (is(n,"NotebookLM")) return notebook(x,t,ar);
        if (is(n,"Character.AI")) return character(x,t,ar);
        if (is(n,"Pi")) return pi(x,t,ar);

        if ("image".equals(style)) return image(x,n,t,ar);
        if ("video".equals(style)) return video(x,n,t,ar);
        if ("voice".equals(style)) return voice(x,n,t,ar);
        if ("music".equals(style)) return music(x,n,t,ar);
        if ("coding".equals(style)) return coding(x,n,t,ar);
        if ("research".equals(style)) return research(x,n,t,ar);
        if ("marketing".equals(style)) return marketing(x,n,t,ar);
        if ("writing".equals(style)) return writing(x,n,t,ar);
        if ("presentations".equals(style) || "presentation".equals(style)) return presentation(x,n,t,ar);
        if ("design".equals(style)) return design(x,n,t,ar);
        if ("3d".equals(style)) return threeD(x,n,t,ar);
        if ("productivity".equals(style)) return productivity(x,n,t,ar);
        return general(x,n,t,ar);
    }

    private static boolean is(String a,String b){return b.equalsIgnoreCase(a);}

    private static String chatgpt(String x,String t,boolean ar){
        if(ar) return "أنت مساعد خبير داخل ChatGPT.\n\n"+
                "الهدف:\n"+x+"\n\n"+
                "نوع المهمة:\n"+t+"\n\n"+
                "السياق:\nاستخدم المعلومات المتاحة فقط، واستخرج الافتراضات المؤثرة قبل التنفيذ.\n\n"+
                "القيود:\n- لا تخترع معلومات.\n- إذا كان هناك نقص مؤثر، حدده بوضوح.\n- حافظ على نية المستخدم.\n\n"+
                "المطلوب من الإخراج:\nقدّم النتيجة النهائية مباشرة، منظمة بعناوين أو خطوات أو جدول أو كود بحسب طبيعة المهمة.";
        return "Act as an expert assistant in ChatGPT.\n\n"+
                "Objective:\n"+x+"\n\nTask:\n"+t+"\n\n"+
                "Context:\nUse the available information and identify only assumptions that materially affect execution.\n\n"+
                "Constraints:\n- Do not invent facts.\n- Flag material missing information.\n- Preserve the user's intent.\n\n"+
                "Output requirements:\nReturn the final result directly, using the format best suited to the task.";
    }

    private static String claude(String x,String t,boolean ar){
        if(ar) return "<role>أنت خبير متخصص يعمل داخل Claude.</role>\n"+
                "<context>المستخدم يريد تنفيذ المهمة التالية: "+x+"</context>\n"+
                "<task>"+t+"</task>\n"+
                "<constraints>\n- حلّل السياق قبل التنفيذ.\n- لا تفترض حقائق غير معطاة.\n- حافظ على الدقة والوضوح.\n</constraints>\n"+
                "<output_format>قدّم نتيجة منظمة وقابلة للتنفيذ، واذكر الافتراضات المؤثرة فقط.</output_format>";
        return "<role>You are an expert working inside Claude.</role>\n"+
                "<context>The user wants to accomplish: "+x+"</context>\n"+
                "<task>"+t+"</task>\n"+
                "<constraints>\n- Analyze the supplied context before acting.\n- Do not invent facts.\n- Preserve accuracy and clarity.\n</constraints>\n"+
                "<output_format>Return a structured, actionable result and state only material assumptions.</output_format>";
    }

    private static String gemini(String x,String t,boolean ar){
        if(ar) return "# الهدف\n"+x+"\n\n"+
                "# المهمة\n"+t+"\n\n"+
                "# السياق\nضع المعلومات المتاحة هنا وافصلها بوضوح عن التعليمات.\n\n"+
                "# القيود\n- كن مباشراً ودقيقاً.\n- لا تضف افتراضات غير ضرورية.\n- عرّف أي مصطلح أو معيار غامض.\n\n"+
                "# شكل الإخراج\nحدّد البنية المطلوبة بوضوح، ثم نفّذ المهمة.\n\n"+
                "# التعليمات النهائية\nاعتمد على السياق أعلاه ونفّذ المهمة بأفضل نتيجة ممكنة.";
        return "# Goal\n"+x+"\n\n"+
                "# Task\n"+t+"\n\n"+
                "# Context\nPlace the supplied information here and keep it clearly separated from instructions.\n\n"+
                "# Constraints\n- Be direct and precise.\n- Avoid unnecessary assumptions.\n- Define ambiguous terms or criteria.\n\n"+
                "# Output format\nSpecify the required structure clearly, then execute the task.\n\n"+
                "# Final instruction\nUsing the context above, complete the task with the best possible result.";
    }

    private static String grok(String x,String t,boolean ar){
        return ar
                ? "Grok، نفّذ المهمة التالية بشكل مباشر وغير متكلف:\nالمهمة: "+x+"\nالنوع: "+t+
                  "\nافصل الحقائق عن الرأي، اختصر الحشو، وإذا كانت المعلومة حساسة للزمن فنبّه إلى ضرورة التحقق من حداثتها. أعطني الناتج النهائي بوضوح."
                : "Grok, handle this directly and without filler:\nTask: "+x+"\nType: "+t+
                  "\nSeparate facts from opinion, avoid unnecessary verbosity, and flag time-sensitive claims that need current verification. Return the useful result clearly.";
    }

    private static String copilot(String x,String t,boolean ar){
        return ar
                ? "أنت GitHub Copilot داخل بيئة تطوير.\nالمطلوب: "+x+"\nنوع المهمة: "+t+
                  "\nقبل اقتراح التعديل: افحص بنية المشروع والملفات والـAPIs الموجودة. حافظ على أسلوب المشروع والتوافق. حدّد الملفات المتأثرة، ثم أعطِ كوداً قابلاً للتشغيل واختبارات للحالات المهمة. لا تخترع ملفات أو APIs."
                : "You are GitHub Copilot inside a development workspace.\nRequest: "+x+"\nTask: "+t+
                  "\nBefore changing anything, inspect the project structure, existing files and APIs. Preserve project conventions and compatibility. Identify affected files, provide runnable code and tests for important cases. Never invent files or APIs.";
    }

    private static String deepseek(String x,String t,boolean ar){
        return ar
                ? "أنت مهندس برمجيات ومحلل منطقي داخل DeepSeek.\nالمشكلة: "+x+"\nالمهمة: "+t+
                  "\nحوّل المطلوب إلى متطلبات واضحة، عالج الحالات الحدية، تحقق من صحة الحل، ثم أعطِ التنفيذ النهائي. لا تعرض سلسلة التفكير الداخلية؛ أعطِ الاستنتاجات والخطوات القابلة للتنفيذ فقط."
                : "You are a software engineer and rigorous problem solver in DeepSeek.\nProblem: "+x+"\nTask: "+t+
                  "\nConvert the request into explicit requirements, handle edge cases, validate the solution, then provide the final implementation. Do not expose private chain-of-thought; provide actionable conclusions and steps only.";
    }

    private static String lechat(String x,String t,boolean ar){
        return ar ? "نفّذ في Le Chat:\nالمطلوب: "+x+"\nالمهمة: "+t+"\nأعطِ جواباً دقيقاً ومختصراً مع بنية واضحة، ووسّع التفاصيل فقط عندما تخدم النتيجة."
                   : "Execute in Le Chat:\nRequest: "+x+"\nTask: "+t+"\nGive a precise, concise answer with clear structure; expand only where it improves the result.";
    }

    private static String poe(String x,String t,boolean ar){
        return ar ? "استخدم Poe للوصول إلى أفضل استجابة من النموذج/البوت المستهدف.\nالفكرة: "+x+"\nالمهمة: "+t+"\nاكتب الطلب بشكل مستقل عن نموذج بعينه، وحدد الهدف والسياق والقيود وشكل الإخراج حتى يبقى قابلاً للنقل بين البوتات."
                   : "Use Poe to obtain the best response from the selected bot/model.\nIdea: "+x+"\nTask: "+t+"\nMake the request model-agnostic while explicitly defining objective, context, constraints and output format.";
    }

    private static String meta(String x,String t,boolean ar){
        return ar ? "ساعدني عبر Meta AI في التالي:\n"+x+"\nنوع المهمة: "+t+"\nكن طبيعياً ومباشراً، حافظ على السياق، وقدّم نتيجة عملية بدون تعليمات تقنية غير لازمة."
                   : "Help me with this in Meta AI:\n"+x+"\nTask: "+t+"\nBe natural and direct, preserve context, and provide a practical result without unnecessary technical prompt jargon.";
    }

    private static String notebook(String x,String t,boolean ar){
        return ar ? "اعمل داخل NotebookLM اعتماداً على المصادر التي أرفقتها فقط.\nالسؤال/الفكرة: "+x+"\nالمهمة: "+t+
                "\nاستخرج الأدلة من المصادر، اربط كل ادعاء مهم بمصدره عندما يكون ممكناً، وميّز بوضوح بين ما تقوله المصادر وما هو استنتاج."
                : "Work in NotebookLM using only the sources I provided.\nQuestion/idea: "+x+"\nTask: "+t+
                "\nGround important claims in the supplied sources when possible, and clearly distinguish source evidence from inference.";
    }

    private static String character(String x,String t,boolean ar){
        return ar ? "الشخصية المطلوبة: حافظ على شخصية الحوار وسياقها داخل Character.AI.\nالموقف: "+x+"\nالمهمة: "+t+
                "\nاجعل الرد متسقاً مع الشخصية، طبيعياً وحوارياً، ولا تكسر الشخصية إلا إذا طلب المستخدم ذلك."
                : "Stay in character inside Character.AI.\nSituation: "+x+"\nTask: "+t+
                "\nKeep the character's voice and context consistent, natural and conversational unless the user explicitly requests a break.";
    }

    private static String pi(String x,String t,boolean ar){
        return ar ? "تحدث معي داخل Pi حول التالي: "+x+"\nالمهمة: "+t+"\nكن ودوداً وطبيعياً، اسأل سؤالاً واحداً فقط إذا كان ضرورياً، ثم ساعدني عملياً."
                   : "Talk with me in Pi about: "+x+"\nTask: "+t+"\nBe warm and natural, ask only one necessary clarifying question, then help practically.";
    }

    private static String general(String x,String n,String t,boolean ar){
        return ar ? "أنت مساعد متخصص داخل "+n+".\nالمطلوب: "+x+"\nنوع المهمة: "+t+
                "\nحدّد الهدف والسياق والقيود وشكل الإخراج، ثم نفّذ النتيجة دون حشو."
                : "You are a specialized assistant inside "+n+".\nRequest: "+x+"\nTask: "+t+
                "\nDefine objective, context, constraints and output format, then produce the result without filler.";
    }

    private static String research(String x,String n,String t,boolean ar){
        return (ar?"أنت باحث متخصص يعمل داخل ":"You are a rigorous research assistant working in ")+n+"."+
                (ar?"\nسؤال البحث: ":"\nResearch question: ")+x+"\n"+(ar?"نوع المهمة: ":"Task: ")+t+
                (ar?"\nافصل الحقائق والاستنتاجات والآراء، استخدم الأدلة القابلة للتحقق، لا تخترع المصادر، واذكر ما يحتاج إلى تحقق إضافي.":"\nSeparate facts, inferences and opinions; use verifiable evidence, never invent citations, and flag what needs verification.");
    }

    private static String image(String x,String n,String t,boolean ar){
        if ("Midjourney".equals(n)) return (ar?"حوّل الفكرة إلى Prompt أصلي لـ Midjourney: ":"Create a native Midjourney prompt from: ")+x+
                "\n"+(ar?"المطلوب: الموضوع، البيئة، التكوين، المنظور/العدسة عند الحاجة، الإضاءة، الخامة، المزاج والأسلوب. استخدم معاملات Midjourney فقط عندما تخدم الفكرة، ولا تستخدم Negative Prompt بصيغة Stable Diffusion.":"Include subject, environment, composition, camera/perspective when useful, lighting, material, mood and style. Use Midjourney parameters only when justified; never use Stable Diffusion negative-prompt syntax.")+
                "\n"+t;
        if ("Stable Diffusion".equals(n)) return (ar?"حوّل الفكرة إلى Prompt لـ Stable Diffusion/SDXL:\nPositive Prompt: ":"Create a Stable Diffusion/SDXL prompt:\nPositive Prompt: ")+x+
                "\n"+(ar?"Negative Prompt: تشوهات وتشويش وأخطاء شائعة مناسبة للمشهد. استخدم الأوزان فقط عند الحاجة ولا تستخدم أعلام Midjourney.":"Negative Prompt: scene-appropriate common defects. Use weighting only when useful and never add Midjourney flags.")+
                "\nTask: "+t;
        if ("Ideogram".equals(n)) return (ar?"أنشئ Prompt لـ Ideogram للفكرة التالية: ":"Create an Ideogram prompt for: ")+x+
                (ar?". ركّز على التكوين والنص داخل الصورة عند وجوده، واكتب النص المطلوب حرفياً.":" Focus on composition and any text inside the image; preserve required text exactly.");
        return (ar?"أنشئ Prompt صورة متخصصاً لـ "+n+": ":"Create a platform-specific image prompt for "+n+": ")+x+
                (ar?". حدّد الموضوع والبيئة والتكوين والإضاءة والألوان والخامات والمنظور والأسلوب، ولا تستخدم معاملات منصة أخرى.":" Include subject, environment, composition, lighting, palette, materials, perspective and style; do not import syntax from another platform.");
    }

    private static String video(String x,String n,String t,boolean ar){
        String focus="Runway".equals(n)?"shot design and camera motion":"Google Veo".equals(n)?"cinematic realism and temporal continuity":"Kling AI".equals(n)?"subject consistency and controlled motion":"Pika".equals(n)?"action/effect clarity and timing":"Luma Dream Machine".equals(n)?"cinematic camera movement and natural motion":"Vidu".equals(n)?"character consistency and controlled motion":"LTX Studio".equals(n)?"shot planning and continuity":"motion, camera and temporal order";
        return (ar?"أنشئ Prompt فيديو أصلياً لـ ":"Create a native video prompt for ")+n+"."+
                "\n"+(ar?"الفكرة: ":"Idea: ")+x+"\n"+(ar?"المهمة: ":"Task: ")+t+
                "\n"+(ar?"تركيز المنصة: ":"Platform focus: ")+focus+
                "\n"+(ar?"رتّب الوصف زمنياً: الكادر، حركة الكاميرا، حركة الموضوع، البيئة والإضاءة، ثم الانتقال/النهاية.":"Describe in temporal order: framing, camera movement, subject motion, environment/lighting, then transition or ending.");
    }

    private static String voice(String x,String n,String t,boolean ar){
        return (ar?"أنشئ Prompt صوت/TTS مخصصاً لـ ":"Create a native voice/TTS prompt for ")+n+
                (ar?":\nالنص/الفكرة: ":"\nText/idea: ")+x+"\nTask: "+t+
                (ar?"\nحدّد هوية الصوت، العمر التقريبي، النبرة، السرعة، الإيقاع، العاطفة، النطق، والتوجيه الأدائي.":"\nSpecify voice identity, approximate age, tone, pace, rhythm, emotion, pronunciation and delivery direction.");
    }

    private static String music(String x,String n,String t,boolean ar){
        return (ar?"أنشئ Prompt موسيقى أصلياً لـ ":"Create a native music prompt for ")+n+
                (ar?":\nالفكرة: ":"\nIdea: ")+x+"\nTask: "+t+
                (ar?"\nحدّد النوع والمزاج والسرعة والآلات والبنية والصوت والإنتاج. افصل الكلمات عن وصف الأسلوب إذا كانت مطلوبة.":"\nSpecify genre, mood, tempo, instrumentation, structure, vocal character and production. Separate lyrics from style direction when needed.");
    }

    private static String coding(String x,String n,String t,boolean ar){
        return (ar?"أنت مهندس برمجيات يعمل داخل ":"You are a senior software engineer working inside ")+n+"."+
                (ar?"\nالمطلوب: ":"\nRequest: ")+x+"\nTask: "+t+
                (ar?"\nافحص بنية المشروع والـAPIs الموجودة، حافظ على التوافق، حدّد الملفات المتأثرة، أعطِ كوداً قابلاً للتشغيل واختبارات، ولا تخترع APIs.":"\nInspect existing project structure and APIs, preserve compatibility, identify affected files, provide runnable code and tests, and never invent APIs.");
    }

    private static String marketing(String x,String n,String t,boolean ar){
        return (ar?"أنشئ Prompt تسويق متخصصاً لـ ":"Create a marketing-native prompt for ")+n+"."+
                (ar?"\nالمنتج/الفكرة: ":"\nProduct/idea: ")+x+"\nTask: "+t+
                (ar?"\nحدّد الجمهور والعرض ومرحلة الوعي والقناة والرسالة والنبرة وCTA والقيود، واطلب مخرجات قابلة للقياس.":"\nSpecify audience, offer, awareness stage, channel, message, tone, CTA and constraints; request measurable outputs.");
    }

    private static String writing(String x,String n,String t,boolean ar){
        return (ar?"أنشئ Prompt كتابة/تحرير مخصصاً لـ ":"Create a writing/editing prompt optimized for ")+n+"."+
                (ar?"\nالمحتوى: ":"\nContent: ")+x+"\nTask: "+t+
                (ar?"\nحافظ على المعنى والصوت ما لم يُطلب تغييره، وحدد الجمهور والطول والبنية والنبرة ومعايير الجودة.":"\nPreserve meaning and voice unless change is requested; define audience, length, structure, tone and quality criteria.");
    }

    private static String presentation(String x,String n,String t,boolean ar){
        return (ar?"أنشئ Prompt عرض تقديمي متخصصاً لـ ":"Create a presentation-native prompt for ")+n+"."+
                (ar?"\nالموضوع: ":"\nTopic: ")+x+"\nTask: "+t+
                (ar?"\nحدّد الجمهور والهدف وعدد الشرائح وتسلسل القصة ورسالة كل شريحة والعناصر البصرية.":"\nDefine audience, objective, slide count, narrative sequence, one message per slide and visual elements.");
    }

    private static String design(String x,String n,String t,boolean ar){
        return (ar?"أنشئ Prompt تصميم متخصصاً لـ ":"Create a design-native prompt for ")+n+"."+
                (ar?"\nالفكرة: ":"\nIdea: ")+x+"\nTask: "+t+
                (ar?"\nحدّد الاستخدام النهائي والمقاس والشبكة والتسلسل البصري والألوان والخطوط والمكونات والتباعد.":"\nSpecify end use, dimensions, grid, hierarchy, palette, typography, components and spacing.");
    }

    private static String productivity(String x,String n,String t,boolean ar){
        return (ar?"حوّل الفكرة إلى طلب عملي داخل ":"Turn this into an actionable request inside ")+n+
                (ar?":\n":"\n")+x+"\nTask: "+t+
                (ar?"\nأعطِ خطوات واضحة، قرارات قابلة للتنفيذ، ومخرجات منظمة.":"\nReturn clear steps, actionable decisions and structured outputs.");
    }

    private static String threeD(String x,String n,String t,boolean ar){
        return (ar?"أنشئ Prompt 3D مخصصاً لـ ":"Create a 3D-generation prompt optimized for ")+n+"."+
                (ar?"\nالفكرة: ":"\nIdea: ")+x+"\nTask: "+t+
                (ar?"\nحدّد الهندسة والنسب والخامة وتفاصيل السطح والإضاءة والكاميرا والخلفية وزاوية العرض.":"\nSpecify geometry, proportions, materials, surface detail, lighting, camera, background and view angle.");
    }

    public static String improve(String s,boolean ar){
        String x=s==null?"":s.trim();
        if(x.isEmpty()) x=ar?"اكتب برومبتاً احترافياً لمهمتي.":"Write a professional prompt for my task.";
        return ar
                ?"أعد هندسة البرومبت التالي ليصبح أدق وأكثر قابلية للتنفيذ. استخرج الهدف والسياق والقيود ومعايير الجودة وشكل المخرجات، واحذف التكرار والغموض. حافظ على نية الكاتب ولا تضف متطلبات غير مذكورة.\n\n"+x+"\n\nأخرج النسخة النهائية فقط."
                :"Re-engineer the following prompt for precision and reliable execution. Extract objective, context, constraints, quality criteria and output format; remove ambiguity and repetition. Preserve the author's intent and do not invent requirements.\n\n"+x+"\n\nReturn only the final prompt.";
    }
}