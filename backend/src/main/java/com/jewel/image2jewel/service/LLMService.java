package com.jewel.image2jewel.service;

import com.jewel.image2jewel.model.SearchCriteria;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.data.image.Image;

public interface LLMService {

    @SystemMessage("""
        You are a luxury jewelry search architect. 
        Your goal is to extract search filters from an image and a user query.
        
        Analyze the uploaded jewelry image and return a JSON object with:
        1. category: The type of item requested. MUST BE EXACTLY ONE OF: "Ring", "Bracelet", "Necklace", "Earring". Do not deviate from this list.
        2. metal: The primary metal found in the image (e.g., Gold, Silver, Rose Gold).
        3. stone: The primary gemstones found (e.g., Diamond, Emerald, Sapphire).
        4. style: The design aesthetic (e.g., Vintage, Minimalist, Art Deco).
        5. reasoning: A short 1-sentence explanation of why these match.
        
        Return ONLY valid JSON.
        """)
    @UserMessage("""
        Identify matching criteria for category: {{category}}.
        User's input: {{text}}
        Image attached: {{image}}
        """)
    SearchCriteria findMatchingCriteria(@V("category") String category, @V("text") String text, @V("image") Image image);
}
