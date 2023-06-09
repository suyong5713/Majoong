package com.example.majoong.map.service;

import com.example.majoong.map.domain.*;
import com.example.majoong.map.dto.RoadDto;
import com.example.majoong.map.repository.*;
import com.example.majoong.tools.CsvUtils;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.geo.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapDataService {
    private final RedisTemplate redisTemplate;
    private final PoliceRepository policeRepository;
    private final StoreRepository storeRepository;
    private final CctvRepository cctvRepository;
    private final BellRepository bellRepository;
    private final LampRepository lampRepository;
    private final SafeRoadRepository safeRoadRepository;

    private final RedisOperations<String, String> redisOperations;

    private final Gson gson;

    public void saveMysqlToRedisGeospatial() {
//        savePoliceToRedis();
//        saveStoreToRedis();
//        saveCctvToRedis();
//        saveBellToRedis();
//        saveLampToRedis();
//        saveSafeRoadToRedis();
        log.info("save success");
    }

    public void saveCsvToMysql() {
//        List<Police> policeList = loadPoliceList();
//        List<Store> storeList = loadStoreList();
//        List<Cctv> cctvList = loadCctvList();
//        List<Bell> bellList = loadBellList();
//        List<Lamp> lampList = loadLampList();
//        List<SafeRoad> safeRoadList = loadSafeRoadList();
        log.info("load success");

//        saveEntity(policeList, policeRepository);
//        saveEntity(storeList, storeRepository);
//        saveEntity(cctvList, cctvRepository);
//        saveEntity(bellList, bellRepository);
//        saveEntity(lampList, lampRepository);
//        saveEntity(safeRoadList, safeRoadRepository);
        log.info("save success");
    }

    public void roadPointCsvToRedis() throws IOException, CsvValidationException {
        Resource resource = new ClassPathResource("road/50mPoint.csv");
        String csvFilePath = resource.getFile().getAbsolutePath();

        CSVReader reader = new CSVReader(new FileReader(csvFilePath));
        String[] line;
        reader.readNext();
        while ((line = reader.readNext()) != null) {
            String roadId = line[0];
            String id = line[3];
            String longitude = line[1];
            String latitude = line[2];
            Point point = new Point(Double.parseDouble(longitude), Double.parseDouble(latitude));
            redisTemplate.opsForGeo().add("50m_road_points", point, roadId+"_"+id);
        }
    }

    public void saveRoadCsvToRedis() throws IOException, CsvValidationException {
        Resource resource = new ClassPathResource("road/riskRoads.csv");
        String csvFilePath = resource.getFile().getAbsolutePath();

        CSVReader reader = new CSVReader(new FileReader(csvFilePath));
        String[] line;
        reader.readNext();
        while ((line = reader.readNext()) != null) {
            String startX = line[1];
            String startY = line[2];
            String endX = line[3];
            String endY = line[4];
            String midX = line[5];
            String midY = line[6];
            String member = startX+"_"+startY+"_"+endX+"_"+endY;

            Point point1 = new Point(Double.parseDouble(startX), Double.parseDouble(startY));
            Point point2 = new Point(Double.parseDouble(endX), Double.parseDouble(endY));
            Point point3 = new Point(Double.parseDouble(midX), Double.parseDouble(midY));

            redisTemplate.opsForGeo().add("risk_roads", point1, member);
            redisTemplate.opsForGeo().add("risk_roads", point2, member);
            redisTemplate.opsForGeo().add("risk_roads", point3, member);

        }
    }
    public void jsonToRedis() throws FileNotFoundException {
        String filePath = "C:/Users/SSAFY/Desktop/S08P31D105/BE/majoong/src/main/resources/road/riskPointList.json";

        FileReader reader = new FileReader(filePath);
        Gson gson = new Gson();

        Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> jsonData = gson.fromJson(reader, mapType);

        for (Map.Entry<String, Object> entry : jsonData.entrySet()) {
            String key = entry.getKey();
            Point point = parseKeyToPoint(key);
            Object value = entry.getValue();
            redisTemplate.opsForGeo().add("risk_road", point, value.toString());
        }

    }

    public void jsonToRedis2() throws FileNotFoundException {
        String filePath = "C:/Users/SSAFY/Desktop/S08P31D105/BE/majoong/src/main/resources/road/riskPolygonList.json";

        FileReader reader = new FileReader(filePath);
        Gson gson = new Gson();

        Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> jsonData = gson.fromJson(reader, mapType);

        for (Map.Entry<String, Object> entry : jsonData.entrySet()) {
            String key = entry.getKey();
            Point point = parseKeyToPoint(key);
            Object value = entry.getValue();
            redisTemplate.opsForGeo().add("risk_polygon", point, value.toString());
        }

    }

    private static Point parseKeyToPoint(String key) {
        // 키를 파싱하여 Point 객체로 변환하는 로직 구현
        String[] parts = key.substring(1, key.length() - 1).split(", ");
        double longitude = Double.parseDouble(parts[0]);
        double latitude = Double.parseDouble(parts[1]);
        return new Point(longitude, latitude);
    }


    public List<RoadDto> getAllRoadPoints() {
        List<RoadDto> points = new ArrayList<>();

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates().includeDistance();
        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = redisOperations.opsForGeo()
                .radius("50m_road_points", new Circle(new Point(128.41915,36.1033), new Distance(9999999999.0, RedisGeoCommands.DistanceUnit.METERS)), args);

        for (GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult : geoResults) {
            RedisGeoCommands.GeoLocation<String> geoLocation = geoResult.getContent();
            Point geoPoint = geoLocation.getPoint();
            double longitude = geoPoint.getX();
            double latitude = geoPoint.getY();
            String[] member = geoResult.getContent().getName().split("_");
            int roadId = Integer.parseInt(member[0]);
            int id =Integer.parseInt(member[1]);
            RoadDto road = new RoadDto(id,roadId,longitude, latitude);
            points.add(road);
        }

        return points;
    }


    public List<RoadDto> findRiskPoints() {
        // 모든 도로 포인트 가져오기
        List<RoadDto> roadPoints = getAllRoadPoints();

        // 처리한 포인트들을 저장하는 Set
        Set<RoadDto> processedPoints = new HashSet<>();

        int len = roadPoints.size();

        List<RoadDto> road = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            RoadDto roadPoint = roadPoints.get(i);

            if (processedPoints.contains(roadPoint)) {
                continue;
            }

            processedPoints.add(roadPoint);

            if (isFacility(roadPoint.getLng(), roadPoint.getLat())) {
                continue;
            }
            road.add(roadPoint);
        }
        return road;
    }


    private boolean isFacility(double x, double y) {
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates().includeDistance();

        GeoResults<RedisGeoCommands.GeoLocation<String>> police = redisOperations.opsForGeo()
                .radius("police", new Circle(new Point(x, y), new Distance(500, RedisGeoCommands.DistanceUnit.METERS)), args);

        GeoResults<RedisGeoCommands.GeoLocation<String>> store = redisOperations.opsForGeo()
                .radius("store", new Circle(new Point(x, y), new Distance(150, RedisGeoCommands.DistanceUnit.METERS)), args);

        GeoResults<RedisGeoCommands.GeoLocation<String>> lamp = redisOperations.opsForGeo()
                .radius("lamp", new Circle(new Point(x, y), new Distance(5, RedisGeoCommands.DistanceUnit.METERS)), args);

        GeoResults<RedisGeoCommands.GeoLocation<String>> cctv = redisOperations.opsForGeo()
                .radius("cctv", new Circle(new Point(x, y), new Distance(10, RedisGeoCommands.DistanceUnit.METERS)), args);

        GeoResults<RedisGeoCommands.GeoLocation<String>> bell = redisOperations.opsForGeo()
                .radius("bell", new Circle(new Point(x, y), new Distance(5, RedisGeoCommands.DistanceUnit.METERS)), args);

        if (!police.getContent().isEmpty() || !store.getContent().isEmpty() || !lamp.getContent().isEmpty()
                || !cctv.getContent().isEmpty()|| !bell.getContent().isEmpty()) {
            return true;
        }
        return false;
    }


    // csv파일에서 Dto List 생성
    public List<Police> loadPoliceList() {
        return CsvUtils.convertToPoliceDtoList("police")
                .stream().map(policeDto -> Police.builder()
                        .policeId(policeDto.getPoliceId())
                        .longitude(policeDto.getLng())
                        .latitude(policeDto.getLat())
                        .address(policeDto.getAddress())
                        .build())
                .collect(Collectors.toList());
    }

    public List<Store> loadStoreList() {
        return CsvUtils.convertToStoreDtoList("store")
                .stream().map(storeDto -> Store.builder()
                        .storeId(storeDto.getStoreId())
                        .longitude(storeDto.getLng())
                        .latitude(storeDto.getLat())
                        .address(storeDto.getAddress())
                        .build())
                .collect(Collectors.toList());
    }

    public List<Cctv> loadCctvList() {
        return CsvUtils.convertToCctvDtoList("cctv")
                .stream().map(cctvDto -> Cctv.builder()
                        .cctvId(cctvDto.getCctvId())
                        .longitude(cctvDto.getLng())
                        .latitude(cctvDto.getLat())
                        .address(cctvDto.getAddress())
                        .build())
                .collect(Collectors.toList());
    }

    public List<Bell> loadBellList() {
        return CsvUtils.convertToBellDtoList("bell")
                .stream().map(bellDto -> Bell.builder()
                        .bellId(bellDto.getBellId())
                        .longitude(bellDto.getLng())
                        .latitude(bellDto.getLat())
                        .address(bellDto.getAddress())
                        .build())
                .collect(Collectors.toList());
    }

    public List<Lamp> loadLampList() {
        return CsvUtils.convertToLampDtoList("lamp")
                .stream().map(lampDto -> Lamp.builder()
                        .lampId(lampDto.getLampId())
                        .longitude(lampDto.getLng())
                        .latitude(lampDto.getLat())
                        .address(lampDto.getAddress())
                        .build())
                .collect(Collectors.toList());
    }

    public List<SafeRoad> loadSafeRoadList() {
        return CsvUtils.convertToSafeRoadDtoList("safeload")
                .stream().map(safeRoadDto -> SafeRoad.builder()
                        .safeRoadId(safeRoadDto.getSafeRoadId())
                        .longitude(safeRoadDto.getLng())
                        .latitude(safeRoadDto.getLat())
                        .address(safeRoadDto.getAddress())
                        .safeRoadNumber(safeRoadDto.getSafeRoadNumber())
                        .build())
                .collect(Collectors.toList());
    }

    // csv에서 mysql에 저장
    public <T> List<T> saveEntity(List<T> entityList, JpaRepository<T, Long> repository) {
        if (CollectionUtils.isEmpty(entityList)) {
            return Collections.emptyList();
        }
        return repository.saveAll(entityList);
    }

    // redis로 저장
    private void savePoliceToRedis() {
        String key = "police";
        List<Police> policeList = policeRepository.findAll();
        for (Police police : policeList) {
            String id = police.getPoliceId().toString();
            String address = police.getAddress();
            Double longitude = police.getLongitude();
            Double latitude = police.getLatitude();
            String member = id + "_" + address;
            if (Objects.isNull(police)) {
                log.error("value가 비었습니다.");
                return;
            }
            try {
                GeoOperations<String, Object> geoOperations = redisTemplate.opsForGeo();
                geoOperations.add(key, new Point(longitude, latitude), member);
//                log.info("저장성공", member);
            } catch (Exception e) {
                log.error("저장실패", e.getMessage());
            }
        }
    }

    private void saveStoreToRedis() {
        String key = "store";
        List<Store> storeList = storeRepository.findAll();
        for (Store store : storeList) {
            String id = store.getStoreId().toString();
            String address = store.getAddress();
            Double longitude = store.getLongitude();
            Double latitude = store.getLatitude();
            String member = id + "_" + address;
            if (Objects.isNull(store)) {
                log.error("value가 비었습니다.");
                return;
            }
            try {
                GeoOperations<String, Object> geoOperations = redisTemplate.opsForGeo();
                geoOperations.add(key, new Point(longitude, latitude), member);
//                log.info("저장성공", member);
            } catch (Exception e) {
                log.error("저장실패", e.getMessage());
            }
        }
    }

    private void saveCctvToRedis() {
        String key = "cctv";
        List<Cctv> cctvList = cctvRepository.findAll();
        for (Cctv cctv : cctvList) {
            String id = cctv.getCctvId().toString();
            String address = cctv.getAddress();
            Double longitude = cctv.getLongitude();
            Double latitude = cctv.getLatitude();
            String member = id + "_" + address;
            if (Objects.isNull(cctv)) {
                log.error("value가 비었습니다.");
                return;
            }
            try {
                GeoOperations<String, Object> geoOperations = redisTemplate.opsForGeo();
                geoOperations.add(key, new Point(longitude, latitude), member);
//                log.info("저장성공", member);
            } catch (Exception e) {
                log.error("저장실패", e.getMessage());
            }
        }
    }

    private void saveBellToRedis() {
        String key = "bell";
        List<Bell> bellList = bellRepository.findAll();
        for (Bell bell : bellList) {
            String id = bell.getBellId().toString();
            String address = bell.getAddress();
            Double longitude = bell.getLongitude();
            Double latitude = bell.getLatitude();
            String member = id + "_" + address;
            if (Objects.isNull(bell)) {
                log.error("value가 비었습니다.");
                return;
            }
            try {
                GeoOperations<String, Object> geoOperations = redisTemplate.opsForGeo();
                geoOperations.add(key, new Point(longitude, latitude), member);
//                log.info("저장성공", member);
            } catch (Exception e) {
                log.error("저장실패", e.getMessage());
            }
        }
    }

    private void saveLampToRedis() {
        String key = "lamp";
        List<Lamp> lampList = lampRepository.findAll();
        for (Lamp lamp : lampList) {
            String id = lamp.getLampId().toString();
            String address = lamp.getAddress();
            Double longitude = lamp.getLongitude();
            Double latitude = lamp.getLatitude();
            String member = id + "_" + address;
            if (Objects.isNull(lamp)) {
                log.error("value가 비었습니다.");
                return;
            }
            try {
                GeoOperations<String, Object> geoOperations = redisTemplate.opsForGeo();
                geoOperations.add(key, new Point(longitude, latitude), member);
//                log.info("저장성공", member);
            } catch (Exception e) {
                log.error("저장실패", e.getMessage());
            }
        }
    }

    private void saveSafeRoadToRedis() {
        String key = "saferoad";
        List<SafeRoad> safeRoadList = safeRoadRepository.findAll();
        for (SafeRoad safeRoad : safeRoadList) {
            String id = safeRoad.getSafeRoadId().toString();
            String address = safeRoad.getAddress();
            Double longitude = safeRoad.getLongitude();
            Double latitude = safeRoad.getLatitude();
            String number = safeRoad.getSafeRoadNumber().toString();
            String member = id + "_" + address + "_" + number;
            if (Objects.isNull(safeRoad)) {
                log.error("value가 비었습니다.");
                return;
            }
            try {
                GeoOperations<String, Object> geoOperations = redisTemplate.opsForGeo();
                geoOperations.add(key, new Point(longitude, latitude), member);
//                log.info("저장성공", member);
            } catch (Exception e) {
                log.error("저장실패", e.getMessage());
            }
        }
    }
}
