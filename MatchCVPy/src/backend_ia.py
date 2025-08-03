import json 
from flask import Flask, request, jsonify
import requests
import spacy
from spacy.matcher import PhraseMatcher
import time

app = Flask(__name__)
print("Carregando modelo spaCy...")
nlp = spacy.load("pt_core_news_lg")

print("Carregando lista de competências...")
try:
    with open("competencias.txt", "r", encoding="utf-8") as f:
        termos_competencias = [line.strip() for line in f if line.strip()]
except FileNotFoundError:
    print("ERRO: Arquivo 'competencias.txt' não encontrado.")
    termos_competencias = []

matcher = PhraseMatcher(nlp.vocab, attr="LOWER")
patterns = [nlp.make_doc(text) for text in termos_competencias]
matcher.add("COMPETENCIAS", patterns)

PROMPT_HIBRIDO_TEMPLATE = """
Você é um Robô Analisador de Currículos. Sua tarefa é analisar o currículo completo para avaliar o nível de proficiência para uma lista específica de competências.

**Contexto:**
- **Currículo Completo:**
---
{texto_curriculo}
---
- **Lista de Competências para Avaliar:** {lista_competencias}

**Sua Tarefa:**
Com base no currículo completo, avalie cada competência da lista fornecida.
Use SOMENTE uma das seguintes quatro palavras para o nível: 'Iniciante', 'Intermediário', 'Avançado', 'Especialista'.

Retorne sua análise SOMENTE no seguinte formato JSON, incluindo TODAS e APENAS as competências da lista fornecida:
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

@app.route('/analisar', methods=['POST'])
def analisar_curriculo():
    start_time = time.time()
    
    texto_curriculo = request.data.decode('utf-8')
    if not texto_curriculo:
        return jsonify({"erro": "Nenhum texto de currículo foi fornecido."}), 400

    doc = nlp(texto_curriculo)
    matches = matcher(doc)
    competencias_encontradas = sorted(list({doc[start:end].text.lower() for match_id, start, end in matches}))
    
    if not competencias_encontradas:
        return jsonify({"avaliacoes": [], "aviso": "Nenhuma competência da lista foi encontrada no currículo."})

    lista_competencias_str = ", ".join(competencias_encontradas)
    
    prompt_final = PROMPT_HIBRIDO_TEMPLATE.format(
        texto_curriculo=texto_curriculo, 
        lista_competencias=lista_competencias_str
    )
    
    dados_para_ollama = {
        "model": "phi3:mini",
        "format": "json",
        "stream": False,
        "prompt": prompt_final
    }
    
    try:
        response = requests.post("http://localhost:11434/api/generate", json=dados_para_ollama)
        response.raise_for_status()
        
        resposta_completa = response.json()
        string_json = resposta_completa['response']
        resultado_final = json.loads(string_json)
        if 'avaliacoes' in resultado_final and isinstance(resultado_final['avaliacoes'], list):
            avaliacoes_filtradas = [
                avaliacao for avaliacao in resultado_final['avaliacoes'] 
                if isinstance(avaliacao, dict) and avaliacao
            ]
            resultado_final = {"avaliacoes": avaliacoes_filtradas}
        else:
            resultado_final = {"avaliacoes": [], "aviso": "O LLM retornou um formato inesperado."}
        end_time = time.time()
        print(f"Análise completa em {end_time - start_time:.2f} segundos.")
        return jsonify(resultado_final)
    
    except Exception as e:
        print(f"ERRO durante a chamada ao LLM: {e}")
        return jsonify({"erro": "Falha ao processar a análise com o LLM."}), 500


if __name__ == '__main__':
    print("Iniciando servidor Flask na porta 5001...")
    app.run(host='0.0.0.0', port=5001)