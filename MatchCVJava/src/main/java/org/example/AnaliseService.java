package org.example;

import com.google.gson.Gson;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class AnaliseService {

    private static final String API_URL_LOCAL = "http://localhost:5001/analisar-local";
    private static final String API_URL_INTERNET = "http://localhost:5001/analisar-internet";

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
     * Envia o texto e os requisitos para o backend de IA.
     *
     * @param textoDoCurriculo O texto a ser analisado.
     * @param requisitos A lista de competências a procurar.
     * @param tipoIA O tipo de IA a ser usado ("IA Local" ou "IA da Internet").
     * @return A resposta JSON do servidor como uma String.
     */
    public String analisarTextoComIA(String textoDoCurriculo, List<String> requisitos, String tipoIA) throws IOException, InterruptedException {
        System.out.println("Enviando texto e " + requisitos.size() + " requisitos para o backend...");

        Map<String, Object> dadosParaEnvio = Map.of(
                "texto_curriculo", textoDoCurriculo,
                "requisitos", requisitos
        );

        Gson gson = new Gson();
        String corpoJson = gson.toJson(dadosParaEnvio);

        System.out.println("--- ENVIANDO JSON PARA O BACKEND ---");
        System.out.println(corpoJson);
        System.out.println("------------------------------------");

        String url = tipoIA.equals("IA Local (Ollama)") ? API_URL_LOCAL : API_URL_INTERNET;

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMinutes(5))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(5))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(corpoJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Resposta recebida do backend. Status: " + response.statusCode());

        return response.body();
    }
}
