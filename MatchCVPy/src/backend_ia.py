import json 
from flask import Flask, request, jsonify
import requests
import spacy
from spacy.matcher import PhraseMatcher

nlp = spacy.load("pt_core_news_lg")

with open("competencias.txt", "r", encoding="utf-8") as f:
    termos_competencias = [line.strip() for line in f]

matcher = PhraseMatcher(nlp.vocab, attr="LOWER")
patterns = [nlp.make_doc(text) for text in termos_competencias]
matcher.add("COMPETENCIAS", patterns)

@app.route('/analisar', methods=['POST'])
def analisar_curriculo():
    texto_curriculo = request.data.decode('utf-8')
    doc = nlp(texto_curriculo)
    
    matches = matcher(doc)
    competencias_encontradas = set() 
    for match_id, start, end in matches:
        span = doc[start:end]
        competencias_encontradas.add(span.text.lower())
        
    resultados_finais = {"avaliacoes": []}
    
    for competencia in sorted(list(competencias_encontradas)):
        prompt_especifico = f"""
        O texto a seguir é um currículo. Avalie o nível de proficiência do candidato na competência específica: "{competencia}".
        Use APENAS uma das palavras: 'Iniciante', 'Intermediário', 'Avançado', 'Especialista'.
        Se a competência não parecer ser usada profissionalmente, classifique como 'Iniciante'.

        Currículo:
        ---
        {texto_curriculo}
        ---
        Qual o nível para "{competencia}"? Retorne APENAS a palavra da classificação.
        """
        
        dados_para_ollama = {
            "model": "phi3:mini",
            "stream": False,
            "prompt": prompt_especifico
        }
        
        response = requests.post("http://localhost:11434/api/generate", json=dados_para_ollama)
        
        if response.status_code == 200:
            nivel = response.json()['response'].strip()
            resultados_finais["avaliacoes"].append({
                "competencia": competencia,
                "nivel_estimado": nivel,
                "justificativa": "Avaliado por IA com base no contexto do currículo." 
            })

    return jsonify(resultados_finais)