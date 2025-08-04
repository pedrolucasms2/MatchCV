package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AnaliseService {

    // URL do nosso backend Python
    private static final String API_URL = "http://localhost:5001/analisar";

    /**
     * Extrai todo o texto de um arquivo PDF.
     *
     * @param arquivoPDF O arquivo a ser lido.
     * @return Uma String contendo todo o texto do PDF.
     * @throws IOException Se ocorrer um erro ao ler o arquivo.
     */
    public String extrairTextoDePDF(File arquivoPDF) throws IOException {
        System.out.println("Iniciando extração de texto do arquivo: " + arquivoPDF.getName());
        try (PDDocument document = PDDocument.load(arquivoPDF)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String texto = stripper.getText(document);
            System.out.println("Extração de texto concluída.");
            return texto;
        }
    }

    /**
     * Envia o texto extraído para o backend de IA e retorna a resposta JSON.
     *
     * @param textoDoCurriculo O texto a ser analisado.
     * @return A resposta JSON do servidor como uma String.
     * @throws IOException Se ocorrer um erro de rede.
     * @throws InterruptedException Se a conexão for interrompida.
     */
    public String analisarTextoComIA(String textoDoCurriculo) throws IOException, InterruptedException {
        System.out.println("Enviando texto para o backend de IA...");

        // Cria um cliente HTTP moderno
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMinutes(5)) // Aumenta o timeout para aguentar a análise
                .build();

        // Cria a requisição POST com o texto do currículo no corpo
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofMinutes(5)) // Timeout para a requisição inteira
                .header("Content-Type", "text/plain; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(textoDoCurriculo))
                .build();

        // Envia a requisição e espera pela resposta
        // HttpResponse.BodyHandlers.ofString() converte a resposta em uma String
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Resposta recebida do backend. Status: " + response.statusCode());

        // Retorna o corpo da resposta (o JSON)
        return response.body();
    }
}