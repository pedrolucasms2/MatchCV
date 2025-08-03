import json 
from flask import Flask, request, jsonify
import requests

app = Flask(__name__)

# (Apenas a variável PROMPT_TEMPLATE muda. O resto do código continua igual.)

PROMPT_TEMPLATE = """
Você é um robô que analisa currículos. Sua única tarefa é extrair as tecnologias, ferramentas e metodologias (as competências técnicas) e avaliar o nível de proficiência.

Para a chave "competencia", use APENAS o nome da tecnologia ou ferramenta (ex: "Python", "React.js", "Docker", "Scrum"). NÃO inclua verbos ou descrições da ação.

Use SOMENTE uma das seguintes quatro palavras para o nível: 'Iniciante', 'Intermediário', 'Avançado', 'Especialista'.

Retorne sua análise SOMENTE no seguinte formato JSON:
{
  "avaliacoes": [
    {
      "competencia": "Nome da Tecnologia",
      "nivel_estimado": "Uma das quatro palavras da escala",
      "justificativa": "Uma frase curta explicando a escolha do nível, baseada no projeto ou responsabilidade descrita no currículo."
    }
  ]
}

Agora, analise o seguinte texto do currículo:

"""


# (As importações e o PROMPT_TEMPLATE continuam os mesmos)

@app.route('/analisar', methods=['POST'])
def analisar_curriculo():
    try:
        texto_curriculo = request.data.decode('utf-8')
        if not texto_curriculo:
            return jsonify({"erro": "Nenhum texto de currículo foi fornecido."}), 400

        # --- NOVA FORMA DE MONTAR O PROMPT ---
        # Simplesmente juntamos a constante do prompt com o texto do currículo.
        # É à prova de erros de formatação.
        prompt_final = PROMPT_TEMPLATE + texto_curriculo

        # O resto do seu código pode continuar exatamente como na nossa última versão funcional.
        dados_para_ollama = {
            "model": "phi3:mini",
            "format": "json",
            "stream": False,
            "prompt": prompt_final
        }
        
        response_ollama = requests.post("http://localhost:11434/api/generate", json=dados_para_ollama)
        response_ollama.raise_for_status()

        resposta_completa_ollama = response_ollama.json()
        string_json_da_resposta = resposta_completa_ollama['response']
        resultado_final_objeto = json.loads(string_json_da_resposta)
        
        return jsonify(resultado_final_objeto)

    # (os blocos de 'except' continuam os mesmos)
    except requests.exceptions.RequestException as e:
        return jsonify({"erro": f"Não foi possível conectar ao serviço de IA (Ollama). Detalhes: {e}"}), 503
    except KeyError:
        print("KeyError: A resposta do Ollama não continha a chave 'response'. Resposta recebida:")
        print(response_ollama.text)
        return jsonify({"erro": "O serviço de IA retornou uma resposta em um formato inesperado."}), 500
    except json.JSONDecodeError:
        print("JSONDecodeError: O texto retornado pelo modelo não é um JSON válido. Texto recebido:")
        print(string_json_da_resposta)
        return jsonify({"erro": "O modelo de IA não gerou um JSON válido."}), 500
    except Exception as e:
        return jsonify({"erro": f"Ocorreu um erro inesperado no servidor de análise. Detalhes: {e}"}), 500
      
            
if __name__ == '__main__':
    # Usando a porta 5001
    app.run(host='0.0.0.0', port=5001)