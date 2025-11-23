package com.upc.tp1inventory.ServiceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upc.tp1inventory.DTO.*;
import com.upc.tp1inventory.Service.MLServiceIntegration;
import com.upc.tp1inventory.Service.PredictionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class MLServiceIntegrationImpl implements MLServiceIntegration {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PredictionService predictionService;

    @Value("${ml.service.url:http://localhost:8000}")
    private String mlServiceUrl;

    public MLServiceIntegrationImpl(RestTemplate restTemplate,
                                    ObjectMapper objectMapper,
                                    PredictionService predictionService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.predictionService = predictionService;
    }

    @Override
    public boolean isMLServiceHealthy() {
        try {
            String url = mlServiceUrl + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            System.err.println("Error verificando salud del servicio ML: " + e.getMessage());
            return false;
        }
    }

    @Override
    public MLServiceHealthDTO getMLServiceHealth() {
        MLServiceHealthDTO dto = new MLServiceHealthDTO();

        try {
            String url = mlServiceUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());

                dto.setStatus(root.has("status") ? root.get("status").asText() : "ok");
                dto.setService(root.has("service") ? root.get("service").asText() : "ML Service");
                dto.setVersion(root.has("version") ? root.get("version").asText() : "1.0.0");
                dto.setMessage("Servicio de ML funcionando correctamente");
            } else {
                dto.setStatus("error");
                dto.setMessage("Servicio respondió con código: " + response.getStatusCode());
            }

        } catch (Exception e) {
            dto.setStatus("unavailable");
            dto.setMessage("Error conectando con el servicio: " + e.getMessage());
        }

        return dto;
    }

    @Override
    public MLTrainResponseDTO trainModel(MLTrainRequestDTO request) {
        MLTrainResponseDTO response = new MLTrainResponseDTO();

        try {
            String url = mlServiceUrl + "/ml/train";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("created_by", request.getCreatedBy());
            requestBody.put("async_mode", request.getAsyncMode() != null ? request.getAsyncMode() : false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> httpResponse = restTemplate.postForEntity(url, httpRequest, String.class);

            if (httpResponse.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(httpResponse.getBody());

                response.setModelId(root.has("model_id") ? root.get("model_id").asText() : null);
                response.setTrainingHistoryId(root.has("training_history_id") ?
                        root.get("training_history_id").asText() : null);
                response.setModelPath(root.has("model_path") ? root.get("model_path").asText() : null);
                response.setVersion(root.has("version") ? root.get("version").asText() : null);
                response.setCreatedBy(root.has("created_by") ? root.get("created_by").asText() : null);

                // Extraer métricas
                if (root.has("metrics")) {
                    JsonNode metrics = root.get("metrics");
                    Map<String, Object> metricsMap = objectMapper.convertValue(metrics, Map.class);
                    response.setMetrics(metricsMap);
                }

                response.setSuccess(true);
                response.setMessage("Modelo entrenado exitosamente");
            } else {
                response.setSuccess(false);
                response.setMessage("Error en entrenamiento: " + httpResponse.getStatusCode());
            }

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error llamando al servicio de entrenamiento: " + e.getMessage());
            throw new RuntimeException("Error en entrenamiento ML: " + e.getMessage(), e);
        }

        return response;
    }

    @Override
    public MLPredictionResponseDTO generatePredictions(MLPredictionRequestDTO request) {
        MLPredictionResponseDTO response = new MLPredictionResponseDTO();

        try {
            String url = mlServiceUrl + "/ml/predict";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("dish_id", request.getDishId() != null ? request.getDishId().toString() : null);
            requestBody.put("weeks_ahead", request.getWeeksAhead() != null ? request.getWeeksAhead() : 4);
            requestBody.put("save_to_db", request.getSaveToDb() != null ? request.getSaveToDb() : true);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> httpResponse = restTemplate.postForEntity(url, httpRequest, String.class);

            if (httpResponse.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(httpResponse.getBody());

                response.setSuccess(root.has("success") ? root.get("success").asBoolean() : true);
                response.setModelId(root.has("model_id") ? root.get("model_id").asText() : null);
                response.setTotalPredictions(root.has("total_predictions") ?
                        root.get("total_predictions").asInt() : 0);

                // Extraer predicciones
                if (root.has("predictions")) {
                    JsonNode predictions = root.get("predictions");
                    List<MLServicePredictionDTO> predictionList = new ArrayList<>();

                    if (predictions.isArray()) {
                        for (JsonNode pred : predictions) {
                            MLServicePredictionDTO dto = new MLServicePredictionDTO();
                            dto.setDishId(pred.has("dish_id") ? pred.get("dish_id").asText() : null);
                            dto.setDishName(pred.has("dish_name") ? pred.get("dish_name").asText() : null);
                            dto.setWeekStart(pred.has("week_start") ? pred.get("week_start").asText() : null);
                            dto.setPredictedDemand(pred.has("predicted_demand") ?
                                    pred.get("predicted_demand").asDouble() : 0.0);
                            dto.setConfidence(pred.has("confidence") ? pred.get("confidence").asText() : "medium");
                            predictionList.add(dto);
                        }
                    }

                    response.setPredictions(predictionList);
                }

                response.setMessage("Predicciones generadas exitosamente");
            } else {
                response.setSuccess(false);
                response.setMessage("Error generando predicciones: " + httpResponse.getStatusCode());
            }

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error llamando al servicio de predicción: " + e.getMessage());
            throw new RuntimeException("Error generando predicciones ML: " + e.getMessage(), e);
        }

        return response;
    }

    @Override
    public List<MLServicePredictionDTO> getPredictionsForDish(UUID dishId, Integer weeksAhead) {
        List<MLServicePredictionDTO> predictions = new ArrayList<>();

        try {
            String url = mlServiceUrl + "/ml/predict/" + dishId + "?weeks_ahead=" +
                    (weeksAhead != null ? weeksAhead : 4);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode predictionsNode = root.get("predictions");

                if (predictionsNode != null && predictionsNode.isArray()) {
                    for (JsonNode pred : predictionsNode) {
                        MLServicePredictionDTO dto = new MLServicePredictionDTO();
                        dto.setDishId(pred.has("dish_id") ? pred.get("dish_id").asText() : null);
                        dto.setDishName(pred.has("dish_name") ? pred.get("dish_name").asText() : null);
                        dto.setWeekStart(pred.has("week_start") ? pred.get("week_start").asText() : null);
                        dto.setPredictedDemand(pred.has("predicted_demand") ?
                                pred.get("predicted_demand").asDouble() : 0.0);
                        dto.setConfidence(pred.has("confidence") ? pred.get("confidence").asText() : "medium");
                        predictions.add(dto);
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo predicciones para plato: " + e.getMessage(), e);
        }

        return predictions;
    }

    @Override
    public List<MLServicePredictionDTO> getLatestPredictions() {
        List<MLServicePredictionDTO> predictions = new ArrayList<>();

        try {
            String url = mlServiceUrl + "/ml/predictions/latest";

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode predictionsNode = root.get("predictions");

                if (predictionsNode != null && predictionsNode.isArray()) {
                    for (JsonNode pred : predictionsNode) {
                        MLServicePredictionDTO dto = new MLServicePredictionDTO();
                        dto.setDishId(pred.has("dish_id") ? pred.get("dish_id").asText() : null);
                        dto.setDishName(pred.has("dish_name") ? pred.get("dish_name").asText() : null);
                        dto.setWeekStart(pred.has("week_start") ? pred.get("week_start").asText() : null);
                        dto.setPredictedDemand(pred.has("predicted_demand") ?
                                pred.get("predicted_demand").asDouble() : 0.0);
                        dto.setConfidence(pred.has("confidence") ? pred.get("confidence").asText() : "medium");
                        predictions.add(dto);
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo predicciones: " + e.getMessage(), e);
        }

        return predictions;
    }

    @Override
    public MLModelInfoDTO getActiveModelInfo() {
        MLModelInfoDTO info = new MLModelInfoDTO();

        try {
            String url = mlServiceUrl + "/ml/models/active";

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());

                info.setModelId(root.has("model_id") ? root.get("model_id").asText() : null);
                info.setModelName(root.has("model_name") ? root.get("model_name").asText() : null);
                info.setModelType(root.has("model_type") ? root.get("model_type").asText() : null);
                info.setVersion(root.has("version") ? root.get("version").asText() : null);

                if (root.has("mae")) {
                    info.setMae(BigDecimal.valueOf(root.get("mae").asDouble()));
                }
                if (root.has("rmse")) {
                    info.setRmse(BigDecimal.valueOf(root.get("rmse").asDouble()));
                }
                if (root.has("accuracy") && !root.get("accuracy").isNull()) {
                    info.setAccuracy(BigDecimal.valueOf(root.get("accuracy").asDouble()));
                }

                info.setTrainedAt(root.has("trained_at") ? root.get("trained_at").asText() : null);
                info.setCreatedAt(root.has("created_at") ? root.get("created_at").asText() : null);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo info del modelo: " + e.getMessage(), e);
        }

        return info;
    }

    @Override
    public MLPredictionResponseDTO syncPredictionsToDatabase(Integer weeksAhead) {
        // 1. Generar predicciones en Python (con save_to_db=true)
        MLPredictionRequestDTO request = new MLPredictionRequestDTO();
        request.setDishId(null); // Todos los platos
        request.setWeeksAhead(weeksAhead != null ? weeksAhead : 4);
        request.setSaveToDb(true); // Python guardará en su BD

        MLPredictionResponseDTO response = generatePredictions(request);

        // 2. Opcionalmente, también guardar en la BD de Spring Boot usando PredictionService
        // (Si quieres mantener duplicadas las predicciones en ambas BDs)

        if (response.getSuccess() && response.getPredictions() != null) {
            // Aquí podrías convertir MLServicePredictionDTO a PredictionDTO
            // y guardarlas usando predictionService.bulkCreate()
            // Por ahora, Python ya las guardó en su BD
        }

        return response;
    }
}