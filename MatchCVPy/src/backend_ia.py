import json 
from flask import Flask, request, jsonify
import requests
import spacy
import time
from gemini_api_service import analisar_com_gemini 

app = Flask(__name__)
print("Carregando modelo spaCy base...")
nlp = spacy.load("pt_core_news_lg")

PROMPT_HIBRIDO_TEMPLATE_OLLAMA = """
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

@app.route('/analisar-local', methods=['POST'])
def analisar_curriculo_local():
    start_time = time.time()
        
    print("\n--- NOVA REQUISIÇÃO RECEBIDA (Ollama) ---")
    
    try:
        dados_recebidos = request.json
    except Exception as e:
        print("!!! ERRO AO PARSEAR JSON:", e)
        return jsonify({"erro": "O corpo da requisição não é um JSON válido."}), 400

    texto_curriculo = dados_recebidos.get('texto_curriculo')
    requisitos = dados_recebidos.get('requisitos', [])

    if not texto_curriculo or not requisitos:
        print("!!! DADOS FALTANDO. Retornando erro.")
        return jsonify({"erro": "Texto do currículo ou lista de requisitos faltando."}), 400

    matcher = spacy.matcher.PhraseMatcher(nlp.vocab, attr="LOWER")
    patterns = [nlp.make_doc(text) for text in requisitos]
    matcher.add("REQUISITOS_VAGA", patterns)
    
    doc = nlp(texto_curriculo)
    matches = matcher(doc)
    competencias_encontradas = sorted(list({doc[start:end].text.lower() for match_id, start, end in matches}))
    
    if not competencias_encontradas:
        return jsonify({"avaliacoes": [], "aviso": "Nenhum dos requisitos foi encontrado no currículo."})

    lista_competencias_str = ", ".join(competencias_encontradas)
        
    prompt_com_texto = PROMPT_HIBRIDO_TEMPLATE_OLLAMA.replace("{texto_curriculo}", texto_curriculo)
    prompt_final = prompt_com_texto.replace("{lista_competencias_encontradas}", lista_competencias_str)
    
    dados_para_ollama = {"model": "phi3:mini", "format": "json", "stream": False, "prompt": prompt_final}
    
    try:
        response = requests.post("http://localhost:11434/api/generate", json=dados_para_ollama)
        response.raise_for_status()
        
        string_json = response.json().get('response', '{}')
        
        print("\n--- STRING JSON RECEBIDA DO LLM (Ollama) (ANTES DO PARSE) ---")
        print(string_json)
        print("----------------------------------------------------------\n")

        resultado_bruto = json.loads(string_json)
        
        resultado_final = {"avaliacoes": []}
        if 'avaliacoes' in resultado_bruto and isinstance(resultado_bruto['avaliacoes'], list):
            avaliacoes_filtradas = [
                aval for aval in resultado_bruto['avaliacoes'] 
                if isinstance(aval, dict) and aval and aval.get("competencia") is not None
            ]
            resultado_final["avaliacoes"] = avaliacoes_filtradas
        
        end_time = time.time()
        print(f"Análise local completa em {end_time - start_time:.2f} segundos.")
        
        return jsonify(resultado_final)
        
    except Exception as e:
        print(f"ERRO durante a chamada ao Ollama: {e}")
        return jsonify({"erro": "Falha ao processar a análise com o Ollama."}), 500

@app.route('/analisar-internet', methods=['POST'])
def analisar_curriculo_internet():
    start_time = time.time()
        
    print("\n--- NOVA REQUISIÇÃO RECEBIDA (Gemini) ---")
    
    try:
        dados_recebidos = request.json
    except Exception as e:
        print("!!! ERRO AO PARSEAR JSON:", e)
        return jsonify({"erro": "O corpo da requisição não é um JSON válido."}), 400

    texto_curriculo = dados_recebidos.get('texto_curriculo')
    requisitos = dados_recebidos.get('requisitos', [])

    if not texto_curriculo or not requisitos:
        print("!!! DADOS FALTANDO. Retornando erro.")
        return jsonify({"erro": "Texto do currículo ou lista de requisitos faltando."}), 400

    matcher = spacy.matcher.PhraseMatcher(nlp.vocab, attr="LOWER")
    patterns = [nlp.make_doc(text) for text in requisitos]
    matcher.add("REQUISITOS_VAGA", patterns)
    
    doc = nlp(texto_curriculo)
    matches = matcher(doc)
    competencias_encontradas = sorted(list({doc[start:end].text.lower() for match_id, start, end in matches}))
    
    resultado = analisar_com_gemini(texto_curriculo, competencias_encontradas)
    
    if "erro" in resultado:
        return jsonify(resultado), 500
    
    end_time = time.time()
    print(f"Análise da internet completa em {end_time - start_time:.2f} segundos.")
    
    return jsonify(resultado)

if __name__ == '__main__':
    print("Iniciando servidor Flask na porta 5001")
    app.run(host='0.0.0.0', port=5001)
