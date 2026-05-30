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
import java.util.*;
import java.util.stream.Collectors;

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

                dto.setStatus(getText(root, "status", "ok"));
                dto.setService(getText(root, "service", "GestRest AI ML Service"));
                dto.setVersion(getText(root, "version", "1.0.0"));
                dto.setMessage("Servicio de ML funcionando correctamente");
            } else {
                dto.setStatus("error");
                dto.setMessage("Servicio respondió con código: " + response.getStatusCode());
            }

        } catch (Exception e) {
            dto.setStatus("unavailable");
            dto.setService("GestRest AI ML Service");
            dto.setVersion("1.0.0");
            dto.setMessage("Error conectando con el servicio ML: " + e.getMessage());
        }

        return dto;
    }

    @Override
    public MLTrainResponseDTO trainModel(MLTrainRequestDTO request) {
        MLTrainResponseDTO response = new MLTrainResponseDTO();

        try {
            String url = mlServiceUrl + "/ml/train";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("start_date", request.getStartDate());
            requestBody.put("end_date", request.getEndDate());
            requestBody.put("fast_mode", request.getFastMode() != null ? request.getFastMode() : true);
            requestBody.put("register_in_db", request.getRegisterInDb() != null ? request.getRegisterInDb() : true);
            requestBody.put("created_by", request.getCreatedBy() != null ? request.getCreatedBy() : "ADMIN");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> httpResponse = restTemplate.postForEntity(url, httpRequest, String.class);

            if (httpResponse.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(httpResponse.getBody());
                JsonNode data = extractDataNode(root);

                response.setModelId(getTextOrNull(data, "model_id"));
                response.setTrainingHistoryId(getTextOrNull(data, "history_id"));
                response.setModelPath(getTextOrNull(data, "model_path"));
                response.setVersion(getTextOrNull(data, "version"));
                response.setCreatedBy(request.getCreatedBy());

                Map<String, Object> metrics = new HashMap<>();

                if (data.has("cv_summary") && data.get("cv_summary").isObject()) {
                    metrics.put("cv_summary", objectMapper.convertValue(data.get("cv_summary"), Map.class));
                }

                if (data.has("train_metrics_positive") && data.get("train_metrics_positive").isObject()) {
                    metrics.put("train_metrics_positive",
                            objectMapper.convertValue(data.get("train_metrics_positive"), Map.class));
                }

                if (data.has("train_metrics_all") && data.get("train_metrics_all").isObject()) {
                    metrics.put("train_metrics_all",
                            objectMapper.convertValue(data.get("train_metrics_all"), Map.class));
                }

                response.setMetrics(metrics);
                response.setSuccess(isCompleted(root));
                response.setMessage(getText(root, "message", "Modelo entrenado correctamente"));
            } else {
                response.setSuccess(false);
                response.setMessage("Error en entrenamiento. Código: " + httpResponse.getStatusCode());
            }

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error llamando al servicio de entrenamiento ML: " + e.getMessage());
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
            requestBody.put("created_by", request.getCreatedBy() != null ? request.getCreatedBy() : "ADMIN");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> httpResponse = restTemplate.postForEntity(url, httpRequest, String.class);

            if (httpResponse.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(httpResponse.getBody());
                JsonNode data = extractDataNode(root);

                response.setSuccess(isCompleted(root));
                response.setModelId(getTextOrNull(data, "model_id"));
                response.setMessage(getText(root, "message", "Predicción generada correctamente"));

                List<MLServicePredictionDTO> predictionList = new ArrayList<>();

                JsonNode predictions = data.get("predictions");

                if (predictions != null && predictions.isArray()) {
                    for (JsonNode pred : predictions) {
                        predictionList.add(toMLServicePredictionDTO(pred));
                    }
                }

                response.setPredictions(predictionList);
                response.setTotalPredictions(predictionList.size());

            } else {
                response.setSuccess(false);
                response.setTotalPredictions(0);
                response.setMessage("Error generando predicciones. Código: " + httpResponse.getStatusCode());
            }

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage("Error llamando al servicio de predicción ML: " + e.getMessage());
            throw new RuntimeException("Error generando predicciones ML: " + e.getMessage(), e);
        }

        return response;
    }

    @Override
    public List<MLServicePredictionDTO> getPredictionsForDish(UUID dishId, Integer weeksAhead) {
        MLPredictionRequestDTO request = new MLPredictionRequestDTO();
        request.setDishId(dishId);
        request.setWeeksAhead(weeksAhead != null ? weeksAhead : 4);
        request.setSaveToDb(false);
        request.setCreatedBy("ADMIN");

        MLPredictionResponseDTO response = generatePredictions(request);

        if (response.getPredictions() == null) {
            return List.of();
        }

        return response.getPredictions();
    }

    @Override
    public List<MLServicePredictionDTO> getLatestPredictions() {
        try {
            return predictionService.getAll()
                    .stream()
                    .sorted((a, b) -> {
                        if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                        if (a.getCreatedAt() == null) return 1;
                        if (b.getCreatedAt() == null) return -1;
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    })
                    .limit(50)
                    .map(this::toMLServicePredictionDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo predicciones guardadas: " + e.getMessage(), e);
        }
    }

    @Override
    public MLModelInfoDTO getActiveModelInfo() {
        MLModelInfoDTO info = new MLModelInfoDTO();

        try {
            String url = mlServiceUrl + "/ml/model/active";

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode data = extractDataNode(root);

                info.setModelId(getTextOrNull(data, "id"));
                info.setModelName(getTextOrNull(data, "model_name"));
                info.setModelType(getTextOrNull(data, "model_type"));
                info.setVersion(getTextOrNull(data, "version"));

                if (data.has("mae") && !data.get("mae").isNull()) {
                    info.setMae(BigDecimal.valueOf(data.get("mae").asDouble()));
                }

                if (data.has("rmse") && !data.get("rmse").isNull()) {
                    info.setRmse(BigDecimal.valueOf(data.get("rmse").asDouble()));
                }

                if (data.has("r2") && !data.get("r2").isNull()) {
                    info.setR2(BigDecimal.valueOf(data.get("r2").asDouble()));
                }

                info.setTrainedAt(getTextOrNull(data, "trained_at"));
                info.setCreatedAt(getTextOrNull(data, "created_at"));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo información del modelo activo: " + e.getMessage(), e);
        }

        return info;
    }

    @Override
    public MLPredictionResponseDTO syncPredictionsToDatabase(Integer weeksAhead) {
        MLPredictionRequestDTO request = new MLPredictionRequestDTO();
        request.setDishId(null);
        request.setWeeksAhead(weeksAhead != null ? weeksAhead : 4);
        request.setSaveToDb(true);
        request.setCreatedBy("ADMIN");

        return generatePredictions(request);
    }

    private JsonNode extractDataNode(JsonNode root) {
        if (root != null && root.has("data") && root.get("data").isObject()) {
            return root.get("data");
        }
        return root;
    }

    private boolean isCompleted(JsonNode root) {
        if (root == null || !root.has("status")) {
            return true;
        }

        String status = root.get("status").asText();

        return "completed".equalsIgnoreCase(status)
                || "ok".equalsIgnoreCase(status)
                || "success".equalsIgnoreCase(status);
    }

    private String getText(JsonNode node, String field, String defaultValue) {
        if (node != null && node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText();
        }
        return defaultValue;
    }

    private String getTextOrNull(JsonNode node, String field) {
        if (node != null && node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText();
        }
        return null;
    }

    private MLServicePredictionDTO toMLServicePredictionDTO(JsonNode pred) {
        MLServicePredictionDTO dto = new MLServicePredictionDTO();

        dto.setDishId(getTextOrNull(pred, "dish_id"));
        dto.setDishName(getTextOrNull(pred, "dish_name"));

        String predictedDate = getTextOrNull(pred, "predicted_date");
        String weekStart = getTextOrNull(pred, "week_start");

        dto.setWeekStart(predictedDate != null ? predictedDate : weekStart);

        Double predictedDemand = null;

        if (pred.has("predicted_quantity") && !pred.get("predicted_quantity").isNull()) {
            predictedDemand = pred.get("predicted_quantity").asDouble();
        } else if (pred.has("predicted_demand") && !pred.get("predicted_demand").isNull()) {
            predictedDemand = pred.get("predicted_demand").asDouble();
        }

        dto.setPredictedDemand(predictedDemand != null ? predictedDemand : 0.0);

        if (pred.has("confidence_level") && !pred.get("confidence_level").isNull()) {
            dto.setConfidence(toConfidenceLabel(BigDecimal.valueOf(pred.get("confidence_level").asDouble())));
        } else if (pred.has("confidence") && !pred.get("confidence").isNull()) {
            dto.setConfidence(pred.get("confidence").asText());
        } else {
            dto.setConfidence("medium");
        }

        return dto;
    }

    private MLServicePredictionDTO toMLServicePredictionDTO(PredictionDTO pred) {
        MLServicePredictionDTO dto = new MLServicePredictionDTO();

        dto.setDishId(pred.getDishId() != null ? pred.getDishId().toString() : null);
        dto.setDishName(pred.getDishName());
        dto.setWeekStart(pred.getPredictedDate() != null ? pred.getPredictedDate().toString() : null);
        dto.setPredictedDemand(pred.getPredictedQuantity() != null ? pred.getPredictedQuantity().doubleValue() : 0.0);
        dto.setConfidence(toConfidenceLabel(pred.getConfidenceLevel()));

        return dto;
    }

    private String toConfidenceLabel(BigDecimal confidenceLevel) {
        if (confidenceLevel == null) {
            return "medium";
        }

        double value = confidenceLevel.doubleValue();

        if (value >= 0.80) {
            return "high";
        }

        if (value >= 0.60) {
            return "medium";
        }

        return "low";
    }
}