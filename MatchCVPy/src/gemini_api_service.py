import requests
import json
import time
from typing import List, Dict, Any
import google.generativeai as genai

PROMPT_HIBRIDO_TEMPLATE = """
Você é um Robô Analisador de Currículos. Sua tarefa é analisar o currículo completo para avaliar o nível de proficiência para uma lista específica de competências.

**Contexto:**
- **Currículo Completo:**
---
{texto_curriculo}
---
- **Lista de Competências para Avaliar:** {lista_competencias_encontradas}

**Sua Tarefa:**
Com base no currículo completo, avalie cada competência da lista fornecida.
Use SOMENTE uma das seguintes quatro palavras para o nível: 'Iniciante', 'Intermediário', 'Avançado', 'Especialista'.

Retorne sua análise SOMENTE no seguinte formato JSON, incluindo TODAS e APENAS as competências da lista fornecida. Garanta que a chave para a competência seja sempre "competencia" (sem acento).
{{
  "avaliacoes": [
    {{
      "competencia": "Nome da Competência da lista",
      "nivel_estimado": "Uma das quatro palavras da escala",
      "justificativa": "Uma frase curta explicando a escolha do nível, baseada no contexto do currículo."
    }}
  ]
}}
"""

GEMINI_API_KEY = "AIzaSyC64KDyiX0PuxdZ3Lhv2UxWYFHl5XEHzQ8"

genai.configure(api_key=GEMINI_API_KEY)

def analisar_com_gemini(texto_curriculo: str, competencias_encontradas: List[str]) -> Dict[str, Any]:
    if not competencias_encontradas:
        return {"avaliacoes": [], "aviso": "Nenhum dos requisitos foi encontrado no currículo."}

    lista_competencias_str = ", ".join(competencias_encontradas)
    
    prompt_com_texto = PROMPT_HIBRIDO_TEMPLATE.replace("{texto_curriculo}", texto_curriculo)
    prompt_final = prompt_com_texto.replace("{lista_competencias_encontradas}", lista_competencias_str)
    
    try:
        start_time = time.time()
        print("\n--- ENVIANDO REQUISIÇÃO PARA O GEMINI ---")
        
        model = genai.GenerativeModel('gemini-2.5-flash')
        response = model.generate_content(
            prompt_final,
            generation_config=genai.GenerationConfig(
                response_mime_type="application/json",
            )
        )
        
        string_json = response.text

        print("\n--- STRING JSON RECEBIDA DO GEMINI ---")
        print(string_json)
        print("---------------------------------------\n")
        
        if string_json.strip().startswith('```json') and string_json.strip().endswith('```'):
            string_json = string_json.strip()[7:-3].strip()

        resultado_bruto = json.loads(string_json)

        resultado_final = {"avaliacoes": []}
        if 'avaliacoes' in resultado_bruto and isinstance(resultado_bruto['avaliacoes'], list):
            avaliacoes_filtradas = [
                aval for aval in resultado_bruto['avaliacoes'] 
                if isinstance(aval, dict) and aval and aval.get("competencia") is not None
            ]
            resultado_final["avaliacoes"] = avaliacoes_filtradas
        
        end_time = time.time()
        print(f"Análise completa em {end_time - start_time:.2f} segundos.")
        
        return resultado_final

    except Exception as e:
        print(f"ERRO durante a chamada à API do Gemini: {e}")
        return {"erro": "Falha na comunicação com a API do Gemini."}
