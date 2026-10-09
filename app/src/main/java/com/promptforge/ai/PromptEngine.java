package com.promptforge.ai;

public final class PromptEngine {
    private PromptEngine() {}

    private static final class Spec {
        final String kind, guidance;
        Spec(String kind,String guidance){this.kind=kind;this.guidance=guidance;}
    }

    public static String generate(String idea, Platform platform, String task, boolean ar) {
        String x=idea==null?"":idea.trim();
        if(x.isEmpty()) x=ar?"حدّد أفضل طريقة لتنفيذ المهمة المطلوبة بدقة.":"Determine the best way to complete the requested task accurately.";
        String n=platform==null?"ChatGPT":platform.name;
        String k=platform==null?"general":platform.style;
        String t=(task==null||task.trim().isEmpty())?"General":task;
        Spec spec=specForTask(t,n,k);
        return render(x,n,t,false,spec);
    }

    private static Spec specForTask(String task,String platform,String fallback){
        String t=task==null?"":task.trim().toLowerCase(java.util.Locale.ROOT);
        if(t.equals("image prompt")||t.equals("image")||t.equals("برومبت صورة")||t.equals("صورة"))
            return new Spec("image","Create a concise, production-ready visual prompt. Specify subject, setting, composition, viewpoint, lighting, color, materials and mood only when useful. Preserve all meaningful details, remove repetition, and never include meta-instructions or prompt-building boilerplate.");
        if(t.equals("video prompt")||t.equals("video")||t.equals("برومبت فيديو")||t.equals("فيديو"))
            return new Spec("video","Specify framing, camera movement, subject action, environmental motion, lighting, continuity and temporal progression. Keep motion physically coherent and omit meta-instructions.");
        if(t.equals("voice / tts")||t.equals("voice")||t.equals("tts")||t.equals("صوت"))
            return new Spec("voice","Specify delivery, tone, pace, rhythm, emphasis, pronunciation and pauses where relevant. Separate spoken copy from performance direction.");
        if(t.equals("coding")||t.equals("programming")||t.equals("برمجة"))
            return new Spec("coding","Define behavior, context, constraints, edge cases, acceptance criteria and verification. Preserve existing APIs and do not invent project details.");
        if(t.equals("research")||t.equals("بحث"))
            return new Spec("research","Use credible evidence, distinguish facts from inference, disclose uncertainty and never invent citations.");
        if(t.equals("marketing")||t.equals("تسويق"))
            return new Spec("marketing","Define audience, objective, channel, message, constraints and desired action.");
        if(t.equals("music")||t.equals("song")||t.equals("موسيقى")||t.equals("أغنية"))
            return new Spec("music","Specify genre, mood, tempo, instrumentation, vocal character and structure when relevant.");
        return specFor(platform,fallback);
    }

    private static String render(String x,String n,String t,boolean ar,Spec s){
        String taskLine="General".equalsIgnoreCase(t)?"":(ar?"\nنوع المهمة: "+t+"\n":"\nTask type: "+t+"\n");
        if("chat".equals(s.kind)) return chat(x,t,ar,s.guidance)+taskLine;
        if("claude".equals(s.kind)) return claude(x,t,ar,s.guidance);
        if("research".equals(s.kind)) return research(x,t,ar,s.guidance);
        if("image".equals(s.kind)) return image(x,t,ar,s.guidance);
        if("video".equals(s.kind)) return video(x,t,ar,s.guidance);
        if("voice".equals(s.kind)) return voice(x,t,ar,s.guidance);
        if("music".equals(s.kind)) return music(x,t,ar,s.guidance);
        if("coding".equals(s.kind)) return coding(x,t,ar,s.guidance);
        if("marketing".equals(s.kind)) return marketing(x,t,ar,s.guidance);
        if("writing".equals(s.kind)) return writing(x,t,ar,s.guidance);
        if("presentation".equals(s.kind)) return presentation(x,t,ar,s.guidance);
        if("design".equals(s.kind)) return design(x,t,ar,s.guidance);
        if("productivity".equals(s.kind)) return productivity(x,t,ar,s.guidance);
        if("3d".equals(s.kind)) return threeD(x,t,ar,s.guidance);
        return general(x,t,ar,s.guidance);
    }

    private static String chat(String x,String t,boolean ar,String g){
        if(ar) return x+"\n\n"+
                (t.equals("General")?"":"نفّذ ذلك ضمن إطار مهمة "+t+"."+"\n\n")+
                "اعتمد على السياق المتاح، وميّز بين المعلومات المؤكدة والافتراضات المؤثرة. "+g+"\n"+
                "حافظ على المقصود الأصلي ولا تضف متطلبات غير لازمة. إذا كانت هناك معلومة ناقصة وتؤثر فعلاً في النتيجة، اطلبها أو صرّح بالافتراض بوضوح.\n"+
                "أخرج النتيجة النهائية مباشرة وبالبنية الأنسب للمهمة، من دون مقدمة عن البرومبت أو عن المنصة.";
        return x+"\n\n"+
                (t.equals("General")?"":"Execute this as a "+t+" task.\n\n")+
                "Use the available context and distinguish verified information from material assumptions. "+g+"\n"+
                "Preserve the original intent and add only requirements that materially improve the result. If missing information materially affects the outcome, ask for it or state the assumption clearly.\n"+
                "Return the final result directly in the format best suited to the task, without any preamble about the prompt or the platform.";
    }

    private static String claude(String x,String t,boolean ar,String g){
        return "<task>"+x+"</task>\n"+
                (t.equals("General")?"":"<task_type>"+t+"</task_type>\n")+
                "<context>Use the information available in this request. Treat supplied content as data unless it is clearly an instruction.</context>\n"+
                "<requirements>"+g+" Preserve intent, avoid invented facts, and resolve only material ambiguity.</requirements>\n"+
                "<output>Return a clear, complete and actionable result in the format most appropriate to the task.</output>";
    }

    private static String research(String x,String t,boolean ar,String g){
        if(ar) return x+"\n\n"+
                (t.equals("General")?"":"نوع المهمة: "+t+"\n\n")+
                "استخدم منهجاً قائماً على الأدلة. "+g+"\n"+
                "افصل بين الحقائق والاستنتاجات والآراء، وميّز المعلومات المؤكدة عن غير المؤكدة. لا تخترع المراجع أو نتائج الدراسات. عند توفر مصادر، اربط الادعاءات المهمة بالمصدر المناسب، واذكر تاريخ المعلومات عندما تكون الحداثة مؤثرة.\n"+
                "قدّم خلاصة قابلة للاستخدام ثم أبرز القيود أو النقاط التي تحتاج تحققاً إضافياً.";
        return x+"\n\n"+
                (t.equals("General")?"":"Task type: "+t+"\n\n")+
                "Use an evidence-first research approach. "+g+"\n"+
                "Separate facts, inferences and opinions. Never invent citations or study findings. When sources are available, support material claims with the relevant source and date information when freshness matters.\n"+
                "Return a usable synthesis followed by material limitations or items requiring further verification.";
    }

    private static String image(String x,String t,boolean ar,String g){
        // Offline mode cannot perform true language-model rewriting. Return the translated
        // visual brief itself rather than polluting it with compiler instructions.
        return x==null?"":x.trim();
    }

    private static String video(String x,String t,boolean ar,String g){
        if(ar) return x+"\n\n"+(t.equals("General")?"":"المهمة: "+t+"\n")+
                g+" رتّب العناصر حسب منطق اللقطة: ما يظهر في الإطار، حركة الموضوع، حركة الكاميرا، البيئة والإضاءة، ثم التسلسل الزمني والنهاية عند الحاجة. استخدم عبارات إيجابية ومباشرة، ولا تضف Negative Prompt إلا إذا كانت الأداة تدعمه صراحةً.";
        return x+"\n\n"+(t.equals("General")?"":"Task: "+t+"\n")+
                g+" Organize the shot around what is visible, subject motion, camera motion, environment and lighting, then temporal progression and ending when useful. Prefer positive, direct language and never add a negative-prompt section unless the target tool explicitly supports it.";
    }

    private static String voice(String x,String t,boolean ar,String g){
        if(ar) return x+"\n\n"+(t.equals("General")?"":"المهمة: "+t+"\n")+
                g+" حدّد أسلوب الإلقاء، السرعة، الإيقاع، النبر، الوقفات والنطق عند الحاجة. لا تدّعِ أن النص يغيّر إعدادات الصوت التي تتحكم بها واجهة الأداة. أبقِ النص المنطوق منفصلاً عن توجيهات الأداء عندما يكون ذلك مفيداً.";
        return x+"\n\n"+(t.equals("General")?"":"Task: "+t+"\n")+
                g+" Specify delivery, pace, rhythm, emphasis, pauses and pronunciation when relevant. Do not pretend the prompt can change voice settings controlled by the tool UI. Keep spoken content distinct from delivery direction when useful.";
    }

    private static String music(String x,String t,boolean ar,String g){
        if(ar) return x+"\n\n"+(t.equals("General")?"":"المهمة: "+t+"\n")+
                g+" حدّد النوع والمزاج والسرعة والآلات والبنية والشخصية الصوتية والإنتاج عند ارتباطها بالمطلوب. عند وجود كلمات أغنية، افصل الكلمات عن وصف الأسلوب بما يتوافق مع واجهة الأداة، ولا تخترع معاملات.";
        return x+"\n\n"+(t.equals("General")?"":"Task: "+t+"\n")+
                g+" Specify genre, mood, tempo, instrumentation, structure, vocal character and production when relevant. When lyrics are supplied, separate lyrics from style direction in the way the tool supports, and do not invent parameters.";
    }

    private static String coding(String x,String t,boolean ar,String g){
        if(ar) return x+"\n\n"+(t.equals("General")?"":"المهمة: "+t+"\n")+
                g+" افحص السياق البرمجي المتاح قبل اقتراح التعديل، وحدد الملفات أو المكونات المتأثرة ومعايير القبول. حافظ على الأنماط والـAPIs الموجودة، واذكر الاختبارات أو خطوات التحقق المناسبة. لا تخترع سياقاً غير متاح ولا تكشف سلسلة التفكير الداخلية.";
        return x+"\n\n"+(t.equals("General")?"":"Task: "+t+"\n")+
                g+" Inspect the available coding context before proposing changes, identify affected files or components and acceptance criteria. Preserve existing conventions and APIs, include appropriate tests or verification steps, and never invent unavailable context or expose private chain-of-thought.";
    }

    private static String marketing(String x,String t,boolean ar,String g){
        if(ar) return x+"\n\n"+(t.equals("General")?"":"المهمة: "+t+"\n")+
                g+" حدّد الجمهور والعرض والمرحلة والرسالة والقناة والنبرة والدعوة إلى الإجراء. اطلب مخرجات قابلة للقياس عندما يكون ذلك مناسباً، واحذف الحشو التسويقي العام.";
        return x+"\n\n"+(t.equals("General")?"":"Task: "+t+"\n")+
                g+" Define audience, offer, funnel or awareness stage, message, channel, tone and CTA. Request measurable outputs when useful and avoid generic marketing filler.";
    }

    private static String writing(String x,String t,boolean ar,String g){
        if(ar) return x+"\n\n"+(t.equals("General")?"":"المهمة: "+t+"\n")+
                g+" حافظ على المعنى وصوت الكاتب ما لم يُطلب خلاف ذلك، وحدد الجمهور والطول والبنية والنبرة ومعايير الجودة عندما تكون مؤثرة.";
        return x+"\n\n"+(t.equals("General")?"":"Task: "+t+"\n")+
                g+" Preserve meaning and author voice unless change is requested, and define audience, length, structure, tone and quality criteria when material.";
    }

    private static String presentation(String x,String t,boolean ar,String g){
        if(ar) return x+"\n\n"+(t.equals("General")?"":"المهمة: "+t+"\n")+
                g+" ابنِ تسلسلاً واضحاً للرسالة، مع تحديد الجمهور والهدف وعدد الشرائح وكثافة المحتوى ورسالة كل شريحة والعناصر البصرية المناسبة.";
        return x+"\n\n"+(t.equals("General")?"":"Task: "+t+"\n")+
                g+" Build a clear narrative sequence with audience, objective, slide count, content density, one core message per slide and appropriate visual direction.";
    }

    private static String design(String x,String t,boolean ar,String g){
        if(ar) return x+"\n\n"+(t.equals("General")?"":"المهمة: "+t+"\n")+
                g+" حدّد الاستخدام النهائي والمقاس والتكوين والتسلسل البصري والخطوط والألوان والمكونات والتباعد والقيود الإنتاجية عندما تكون ذات صلة.";
        return x+"\n\n"+(t.equals("General")?"":"Task: "+t+"\n")+
                g+" Specify end use, dimensions, composition, hierarchy, typography, palette, components, spacing and production constraints when relevant.";
    }

    private static String productivity(String x,String t,boolean ar,String g){
        return x+"\n\n"+(t.equals("General")?"":(ar?"المهمة: ":"Task: ")+t+"\n")+
                (ar?g+" حوّل المطلوب إلى خطوات وقرارات ومخرجات قابلة للتنفيذ، مع الحفاظ على بنية البيانات أو مساحة العمل الحالية.":" "+g+" Turn the request into actionable steps, decisions and structured outputs while preserving the existing workspace or data structure.");
    }

    private static String threeD(String x,String t,boolean ar,String g){
        if(ar) return x+"\n\n"+(t.equals("General")?"":"المهمة: "+t+"\n")+
                g+" حدّد الهندسة والنسب والخامة وتفاصيل السطح والإضاءة والكاميرا والخلفية وزاوية العرض ومستوى التفاصيل المطلوب.";
        return x+"\n\n"+(t.equals("General")?"":"Task: "+t+"\n")+
                g+" Specify geometry, proportions, materials, surface detail, lighting, camera, background, view angle and required detail level.";
    }

    private static String general(String x,String t,boolean ar,String g){
        if(ar) return x+"\n\n"+(t.equals("General")?"":"المهمة: "+t+"\n")+
                g+" حدّد ما هو ضروري فقط، وحافظ على نية المستخدم، وقدّم مخرجاً واضحاً قابلاً للاستخدام.";
        return x+"\n\n"+(t.equals("General")?"":"Task: "+t+"\n")+
                g+" Include only what is necessary, preserve user intent and return a clear, usable output.";
    }

    private static Spec specFor(String n,String fallback){
        if("ChatGPT".equals(n)) return new Spec("chat","Use clear objective, relevant context, explicit constraints, and a concrete output contract. Start with the task rather than a role-play preamble.");
        if("Claude".equals(n)) return new Spec("claude","Use concise XML-style sections to separate task, context, requirements and output. Keep data separate from instructions.");
        if("Gemini".equals(n)) return new Spec("chat","Be precise and direct. Use one consistent structure, define ambiguous parameters, and keep critical instructions near the beginning.");
        if("Grok".equals(n)) return new Spec("chat","Prefer direct natural language, separate facts from opinions, and flag claims that require current verification.");
        if("Perplexity".equals(n)) return new Spec("research","Treat the task as web-grounded research. Ask for current, source-supported findings and transparent uncertainty.");
        if("Copilot".equals(n)) return new Spec("chat","State the desired result clearly and use concise context. When code or repositories are involved, reference the actual workspace context instead of inventing it.");
        if("Meta AI".equals(n)) return new Spec("chat","Use natural conversational wording with sufficient context. Avoid unnecessary prompt-engineering jargon.");
        if("DeepSeek".equals(n)) return new Spec("chat","Define requirements, edge cases, validation criteria and the final deliverable. Never request hidden chain-of-thought.");
        if("Le Chat".equals(n)) return new Spec("chat","Keep the instruction concise, explicit and outcome-focused.");
        if("Poe".equals(n)) return new Spec("chat","Make the request portable across the selected bot while preserving clear objective, context, constraints and output expectations.");
        if("NotebookLM".equals(n)) return new Spec("research","Ground claims in the supplied notebook sources. Distinguish source evidence from inference and do not import unsupported outside facts.");
        if("Character.AI".equals(n)) return new Spec("chat","Preserve character voice, relationship context and conversational continuity. Do not break character unless the request requires it.");
        if("Pi".equals(n)) return new Spec("chat","Use warm, natural conversational language. Ask at most one necessary clarification before helping when ambiguity blocks the task.");

        if("Midjourney".equals(n)) return new Spec("image","Keep the prompt visual and relatively concise. Specify subject, medium, environment, composition, lighting, color and mood; use Midjourney parameters only at the end when they materially help, and never use Stable Diffusion negative-prompt syntax.");
        if("Adobe Firefly".equals(n)) return new Spec("image","Use a natural visual brief covering subject, composition, background, style, lighting, color and intended use. Do not import syntax from other image generators.");
        if("Ideogram".equals(n)) return new Spec("image","Prioritize exact in-image text, typography, layout, subject placement and legibility. Preserve required wording verbatim.");
        if("Leonardo AI".equals(n)) return new Spec("image","Specify subject, composition, style, lighting, materials, detail and realism. Do not invent undocumented flags.");
        if("FLUX".equals(n)) return new Spec("image","Prefer strong natural-language visual description, clear spatial relationships, lighting, style and exact text when needed; avoid foreign platform flags.");
        if("Stable Diffusion".equals(n)) return new Spec("image","Use a clear positive prompt and a separate negative prompt only because this workflow supports that distinction. Describe scene-specific defects in the negative section and do not use Midjourney parameters. Prefer English for best consistency.");
        if("Recraft".equals(n)) return new Spec("image","Treat the request as a design-oriented visual brief. Prioritize style, composition, typography, vector or raster intent, brand constraints and editable-use considerations when relevant.");
        if("Krea".equals(n)) return new Spec("image","State whether the goal is generation, editing or enhancement, then describe the source/reference, desired composition, style and transformation.");
        if("Canva AI".equals(n)) return new Spec("design","Specify content type, dimensions, audience, copy, hierarchy, palette, typography and editable layout intent.");
        if("Freepik AI".equals(n)) return new Spec("image","Use a commercial visual brief with subject, composition, style, lighting, palette, negative space and asset purpose when relevant.");
        if("Magnific".equals(n)) return new Spec("image","Treat the request as enhancement or upscaling: prioritize detail recovery, texture, sharpness and realism while preserving the source instead of inventing a new scene.");

        if("Runway".equals(n)) return new Spec("video","For video, prioritize visible action, subject motion, camera movement and temporal progression. For image-to-video, treat the input image as the composition and use the text prompt primarily to describe motion. Avoid negative prompts.");
        if("Google Veo".equals(n)) return new Spec("video","Prioritize coherent shot design, realistic physics, camera language, subject action, environment, lighting, dialogue or audio only when requested, and temporal continuity.");
        if("Kling AI".equals(n)) return new Spec("video","Prioritize subject identity, controlled movement, physical interaction, camera direction and temporal consistency. Keep each action physically coherent.");
        if("Pika".equals(n)) return new Spec("video","Focus on the visible action or transformation, timing, framing and effect. Keep the instruction concise and unambiguous.");
        if("Luma Dream Machine".equals(n)) return new Spec("video","Prioritize cinematic camera movement, natural motion, spatial continuity and shot progression. Keep subject motion physically plausible.");
        if("Hailuo AI".equals(n)) return new Spec("video","Specify the subject action, camera movement, scene continuity and visual style. Keep temporal order explicit.");
        if("PixVerse".equals(n)) return new Spec("video","Prioritize motion effect, subject consistency, camera direction, framing and timing. Avoid unnecessary prose.");
        if("Haiper".equals(n)) return new Spec("video","Prioritize clear subject motion, camera direction, environment and concise temporal instructions.");
        if("Vidu".equals(n)) return new Spec("video","Prioritize character or object consistency, controlled movement, shot continuity and explicit sequence of actions.");
        if("LTX Studio".equals(n)) return new Spec("video","Treat the request as shot planning: scene, shot purpose, camera, action, dialogue or audio when relevant, transitions and continuity.");

        if("ElevenLabs".equals(n)) return new Spec("voice","Separate spoken text from delivery direction. Cover voice character, emotion, pacing, pronunciation and pauses when relevant; remember voice selection and many controls are platform settings.");
        if("PlayHT".equals(n)) return new Spec("voice","Specify voice style, delivery, pacing, emotion and pronunciation. Keep the spoken script explicit and do not invent provider syntax.");
        if("Cartesia".equals(n)) return new Spec("voice","Specify voice character, emotional state, prosody, pacing and pronunciation, while keeping platform settings separate from the text prompt.");
        if("Fish Audio".equals(n)) return new Spec("voice","Specify voice identity, style, emotion, pacing, pronunciation and exact spoken material, without borrowing syntax from another voice tool.");
        if("Murf".equals(n)) return new Spec("voice","Specify narration role, audience, pace, emphasis, pronunciation and delivery context. Keep the script distinct from performance direction.");
        if("Speechify".equals(n)) return new Spec("voice","Specify reading style, pacing, pronunciation, emphasis and listening context. Do not claim prompt text controls settings that live in the UI.");

        if("Suno".equals(n)) return new Spec("music","Specify genre or style, mood, tempo, instrumentation, vocal character and song structure. Keep lyrics separate when supplied; avoid artist-copying requests and translate them into musical characteristics.");
        if("Udio".equals(n)) return new Spec("music","Use a concise musical description with genre, mood, instrumentation and useful tags. Keep lyrics distinct and use supported guidance or manual-style concepts only when appropriate.");
        if("Stable Audio".equals(n)) return new Spec("music","Describe the audio event or track with genre or mood, instrumentation, texture, structure and duration intent. Do not import Suno or Udio syntax.");

        if("Cursor".equals(n)) return new Spec("coding","Treat this as an agentic repository task: identify context to inspect, relevant files or symbols, requested change, constraints, acceptance criteria, verification and safe stopping conditions. Use @context only when the user actually supplied or selected it.");
        if("GitHub Copilot".equals(n)) return new Spec("coding","Ground the task in the actual repository, symbols, files, pull requests or workspace context available to Copilot. State the requested change and acceptance criteria; ask for tests where appropriate.");
        if("Claude Code".equals(n)) return new Spec("coding","Frame this as an agentic repository change with goals, files or areas to inspect, constraints, implementation requirements, verification commands and stopping conditions.");
        if("Windsurf".equals(n)) return new Spec("coding","Frame the task around workspace context, intended behavior, affected files, project conventions, safe execution and verification.");
        if("Replit".equals(n)) return new Spec("coding","Use current project and runtime context, describe the desired user-visible behavior, implementation constraints and verification.");
        if("Amazon Q Developer".equals(n)) return new Spec("coding","Include project and runtime context plus relevant AWS architecture, resources, security constraints and validation when applicable.");
        if("Gemini Code Assist".equals(n)) return new Spec("coding","Use IDE or project context, selected code or files, desired change, compatibility constraints and tests.");
        if("Tabnine".equals(n)) return new Spec("coding","Provide precise local code context, language or framework constraints, expected behavior and compatibility requirements without inventing hidden project details.");

        if("Jasper".equals(n)) return new Spec("marketing","Define audience, brand voice, campaign objective, offer, channel, funnel stage, format, key message and CTA. Preserve brand constraints.");
        if("Copy.ai".equals(n)) return new Spec("marketing","Frame the request as a structured marketing workflow with audience, offer, funnel stage, channel, copy type, constraints and useful variants.");
        if("Writesonic".equals(n)) return new Spec("marketing","Specify SEO or content objective, target query or audience, content structure, evidence needs, tone and conversion goal.");

        if("Grammarly".equals(n)) return new Spec("writing","Specify the source text, editing goal, audience, desired tone and degree of change. Preserve meaning and any text that must remain unchanged.");
        if("Writer".equals(n)) return new Spec("writing","Specify brand voice, audience, terminology constraints, content objective, structure and quality criteria.");

        if("Notion AI".equals(n)) return new Spec("productivity","Frame this as a workspace task with page or database context, desired transformation, properties or fields when relevant, output structure and final action.");
        if("Gamma".equals(n)) return new Spec("presentation","Define audience, objective, narrative, slide or section count, content density, visual direction and key takeaway.");
        if("Beautiful.ai".equals(n)) return new Spec("presentation","Define presentation goal, audience, slide hierarchy, concise copy, visual consistency and layout intent.");
        if("Tome".equals(n)) return new Spec("presentation","Frame the request as a narrative presentation with audience, story arc, slide intent, visual direction and concise copy.");
        if("Napkin AI".equals(n)) return new Spec("presentation","Define the idea, entities or relationships to visualize, audience and the most useful diagram or visual explanation.");

        if("Framer AI".equals(n)) return new Spec("design","Specify website or page goal, target audience, sections, hierarchy, responsive behavior, copy requirements and visual style.");
        if("Canva Magic Studio".equals(n)) return new Spec("design","Specify the desired creative output, dimensions or format, audience, copy, hierarchy, visual style and editable layout.");

        if("Elicit".equals(n)) return new Spec("research","Define the research question, inclusion or exclusion criteria, evidence fields to extract and synthesis method. Distinguish evidence from interpretation.");
        if("Consensus".equals(n)) return new Spec("research","Ask a precise scientific question and request study-level evidence, direction or strength of findings and limitations. Avoid treating a single paper as universal proof.");
        if("SciSpace".equals(n)) return new Spec("research","Anchor the task to the supplied paper or academic document, specify the section or question, and require evidence extraction with citation-aware explanation.");
        if("You.com".equals(n)) return new Spec("research","Frame as web-assisted research with a clear objective, freshness expectations, source support, uncertainty handling and concise synthesis.");

        if("Meshy".equals(n)) return new Spec("3d","Specify the 3D asset, geometry, proportions, materials, surface detail, topology or use constraints and view requirements.");
        if("Tripo AI".equals(n)) return new Spec("3d","Describe the target 3D object with geometry, proportions, materials, topology or detail level and intended use.");

        if("Hugging Face".equals(n)) return new Spec("general","Specify the task, model or pipeline context if known, input schema, desired output and evaluation target. Never assume every model uses the same prompt format.");
        if("Replicate".equals(n)) return new Spec("general","Specify the task, target model when known, input fields, expected output and constraints. Keep model-specific parameters explicit rather than inventing them.");
        if("OpenRouter".equals(n)) return new Spec("chat","Keep the prompt portable across the selected model. State objective, context, constraints and output format, and only use model-specific behavior when the model is known.");
        if("Together AI".equals(n)) return new Spec("general","Specify model or task context, input requirements, desired output and evaluation criteria. Avoid pretending the provider has one universal prompt syntax.");
        if("Fireworks AI".equals(n)) return new Spec("general","Specify model or task context, structured input and output expectations, constraints and validation requirements without unsupported provider syntax.");
        if("Fal.ai".equals(n)) return new Spec("general","Specify the generation task, model or workflow context, inputs, output requirements and relevant controls without inventing API parameters.");

        if("Photoroom".equals(n)) return new Spec("design","Specify the product or subject, background, composition, lighting, cleanup or replacement goal and commercial asset use while preserving product identity.");
        if("Looka".equals(n)) return new Spec("design","Specify brand name, industry, audience, personality, logo concept, symbol geometry, typography and real-world brand applications.");
        if("Descript".equals(n)) return new Spec("video","Treat the request as an audio/video editing or production task when applicable: transcript or source context, edits, speakers, timing, visual structure and final deliverable.");
        if("OpusClip".equals(n)) return new Spec("video","Treat the request as long-form-to-short-form editing: source context, audience, clip goal, hook, pacing, captions, reframing and platform format.");
        if("Otter.ai".equals(n)) return new Spec("productivity","Frame around meeting or transcript context, speakers, requested extraction, decisions, action items, owners, deadlines and desired output.");
        if("Fireflies.ai".equals(n)) return new Spec("productivity","Frame around meeting intelligence: transcript context, decisions, action items, owners, deadlines, risks and the intended summary format.");
        if("Mem".equals(n)) return new Spec("productivity","Frame as knowledge retrieval or organization: relevant context, desired connection or memory, summary, decision or action and output format.");
        return new Spec(fallback==null||fallback.isEmpty()?"general":fallback,"Understand the user's intent, preserve it, add only material constraints and define a useful output format. Do not invent platform-specific syntax.");
    }

    public static String improve(String s,boolean ar){
        String x=s==null?"":s.trim();
        if(x.isEmpty()) x="Write a professional prompt for my task.";
        return x+"\n\nRe-engineer this prompt for clarity, precision and reliable execution. Remove ambiguity and repetition, add material constraints, quality criteria and an output contract when needed, preserve the author's intent, and do not invent facts or requirements. Return only the final prompt, without explaining the rewrite process.";

}

}
