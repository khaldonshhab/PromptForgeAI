package com.promptforge.ai;
public final class PromptEngine{
 private PromptEngine(){}
 public static String generate(String idea,Platform p,String task,boolean ar){
  String x=idea==null||idea.trim().isEmpty()?(ar?"أنشئ لي نتيجة احترافية لمشروعي":"Create a professional result for my project"):idea.trim();
  if(ar)return "أنت خبير متخصص في "+p.name+" ضمن مجال "+p.category+".\n\nالهدف: "+x+"\n\nنوع المهمة: "+task+"\n\nالتعليمات:\n1) افهم الهدف والسياق.\n2) استخدم تفاصيل دقيقة وقابلة للتنفيذ.\n3) تجنب الغموض والتكرار.\n4) اقترح تحسينات عند الحاجة.\n\nالمخرجات المطلوبة: قدم نتيجة منظمة وجاهزة للنسخ، مع الحفاظ على اللغة والأسلوب المطلوبين.\n\nصيغة الاستخدام: "+p.name;
  return "You are an expert prompt engineer specialized in "+p.name+" ("+p.category+").\n\nGoal: "+x+"\n\nTask: "+task+"\n\nInstructions:\n1) Clarify the objective and context.\n2) Add precise, actionable details.\n3) Avoid ambiguity and redundancy.\n4) Suggest useful improvements when needed.\n\nOutput: Return a clean, production-ready result optimized for "+p.name+".";
 }
 public static String improve(String s,boolean ar){
  String x=s==null?"":s.trim(); if(x.isEmpty())x=ar?"اكتب برومبتاً احترافياً لمهمتي.":"Write a professional prompt for my task.";
  return (ar?"حسّن البرومبت التالي ليكون أكثر دقة ووضوحاً وقابلية للتنفيذ.\n\n":"Improve the following prompt for clarity, precision, and reliable execution.\n\n")+x+"\n\n"+(ar?"أخرج النسخة النهائية فقط، مع تنظيم الهدف والسياق والتعليمات والقيود والمخرجات.":"Return the final optimized prompt with objective, context, instructions, constraints, and output format organized clearly.");
 }
}
