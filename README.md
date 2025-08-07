# MatchCV

![Licença](https://img.shields.io/badge/license-MIT-blue.svg)

MatchCV é uma aplicação de desktop segura e de código aberto projetada para otimizar o processo de recrutamento. Ele utiliza um modelo de Inteligência Artificial local para analisar, filtrar e ranquear currículos, garantindo que os dados sensíveis dos candidatos nunca saiam do seu computador.

---

### Tabela de Conteúdos

*   [Sobre o Projeto](#sobre-o-projeto)
*   [Principais Funcionalidades](#principais-funcionalidades)
*   [Tecnologias Utilizadas](#tecnologias-utilizadas)
*   [Começando](#começando)
*   [Uso](#uso)
*   [Roadmap](#roadmap)
*   [Licença](#licença)

---

### Sobre o Projeto

Em um processo seletivo, analisar dezenas ou centenas de currículos manualmente é uma tarefa demorada e suscetível a vieses. O CV-Ranker resolve esse problema automatizando a triagem inicial.

A principal filosofia do projeto é a **privacidade em primeiro lugar**. Ao processar todos os dados localmente, eliminamos os riscos de segurança associados ao envio de informações de candidatos para serviços de terceiros na nuvem.

---

### Principais Funcionalidades

*   **Análise Local com IA:** Utiliza modelos de linguagem (LLMs) que rodam 100% offline.
*   **Extração Inteligente:** Identifica competências técnicas (hard skills) e comportamentais (soft skills).
*   **Inferência de Proficiência:** Estima o nível de conhecimento (Iniciante, Intermediário, Avançado) para cada competência com base no contexto do currículo.
*   **Ranking Ponderado:** Classifica os candidatos com base em critérios de importância definidos pelo recrutador para cada vaga.
*   **Suporte a Múltiplos Formatos:** Processa arquivos `.pdf` e `.docx`.
*   **Segurança:** Nenhum dado é enviado para a internet.

---

### Tecnologias Utilizadas

*   **Aplicação Principal (Frontend):**
    *   ![Java](https://img.shields.io/badge/Java-17%2B-orange)
    *   ![JavaFX](https://img.shields.io/badge/JavaFX-UI-blue) 
    *   ![Apache Tika](https://img.shields.io/badge/Apache-Tika-lightgrey) (para extração de texto)

*   **Serviço de IA (Backend):**
    *   ![Python](https://img.shields.io/badge/Python-3.9%2B-yellow)
    *   ![Flask](https://img.shields.io/badge/Flask-API-black) 
    *   ![Ollama](https://img.shields.io/badge/Ollama-LLM%20Runner-green)

---

### Começando

Para executar o projeto em um ambiente de desenvolvimento, siga os passos abaixo.

#### Pré-requisitos

1.  **Java Development Kit (JDK)** - Versão 17 ou superior.
2.  **Python** - Versão 3.9 ou superior.
3.  **Ollama** - Siga as instruções de instalação em [ollama.com](https://ollama.com).

#### Instalação

1.  **Baixe o modelo de IA via Ollama:**
    ```sh
    ollama pull phi3:mini
    ```

2.  **Clone o repositório:**
    ```sh
    git clone https://github.com/pedrolucasms2/MatchCV
    cd MatchCV
    ```

3.  **Configure o backend Python:**
    ```sh
    cd MatchCVPy
    pip install -r requirements.txt
    ```

4.  **Execute o backend:**
    ```sh
    python backend_ia.py
    ```
    O servidor de IA estará rodando em `http://localhost:5000`.

5.  **Configure e execute o frontend Java:**
    *   Abra a pasta `MatchCVJava` na sua IDE Java (IntelliJ/Eclipse).
    *   Compile e execute a classe principal da aplicação.

---

### Uso

1.  Inicie o servidor de backend Python.
2.  Inicie a aplicação Java.
3.  Na tela principal, defina as competências e pesos para a vaga desejada.
4.  Clique em "Selecionar Currículos" e escolha os arquivos a serem analisados.
5.  Clique em "Analisar" e aguarde o processamento.
6.  Os resultados aparecerão ranqueados na tabela principal.

---

### Roadmap

Veja o [ROADMAP.md](ROADMAP.md) para detalhes sobre os próximos passos e funcionalidades planejadas. *(Você pode criar um arquivo separado para o roadmap ou incluí-lo aqui).*

---

### Licença

Distribuído sob a licença MIT. Veja `LICENSE` para mais informações.
