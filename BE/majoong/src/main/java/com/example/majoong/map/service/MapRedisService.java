package com.example.majoong.map.service;

import com.example.majoong.map.domain.Bell;
import com.example.majoong.map.domain.Cctv;
import com.example.majoong.map.domain.Police;
import com.example.majoong.map.domain.Store;
import com.example.majoong.map.dto.PoliceDto;
import com.example.majoong.map.repository.BellRepository;
import com.example.majoong.map.repository.CctvRepository;
import com.example.majoong.map.repository.PoliceRepository;
import com.example.majoong.map.repository.StoreRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapRedisService {
    private static final String CACHE_KEY = "POLICE";
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private HashOperations<String, String, String> hashOperations;
    private final PoliceRepository policeRepository;
    private final StoreRepository storeRepository;
    private final CctvRepository cctvRepository;
    private final BellRepository bellRepository;

    @PostConstruct
    public void init() {
        this.hashOperations = redisTemplate.opsForHash();
    }

    public void saveMysqlToRedisGeospatial() {
//        savePoliceToRedis();
//        saveStoreToRedis();
//        saveCctvToRedis();
        saveBellToRedis();
        log.info("save success");
    }

    private void savePoliceToRedis() {
        String key = "police";
        List<Police> policeList = policeRepository.findAll();
        for (Police police : policeList) {
            String id = police.getPoliceId().toString();
            String address = police.getAddress();
            Double longitude = police.getLongitude();
            Double latitude = police.getLatitude();
            String member = id + " : " + address;
            if (Objects.isNull(police)) {
                log.error("value가 비었습니다.");
                return;
            }
            try {
                GeoOperations<String, Object> geoOperations = redisTemplate.opsForGeo();
                geoOperations.add(key, new Point(longitude, latitude), member);
                System.out.println(member);
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
            String member = id + " : " + address;
            if (Objects.isNull(store)) {
                log.error("value가 비었습니다.");
                return;
            }
            try {
                GeoOperations<String, Object> geoOperations = redisTemplate.opsForGeo();
                geoOperations.add(key, new Point(longitude, latitude), member);
                System.out.println(member);
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
            String member = id + " : " + address;
            if (Objects.isNull(cctv)) {
                log.error("value가 비었습니다.");
                return;
            }
            try {
                GeoOperations<String, Object> geoOperations = redisTemplate.opsForGeo();
                geoOperations.add(key, new Point(longitude, latitude), member);
                System.out.println(member);
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
            String member = id + " : " + address;
            if (Objects.isNull(bell)) {
                log.error("value가 비었습니다.");
                return;
            }
            try {
                GeoOperations<String, Object> geoOperations = redisTemplate.opsForGeo();
                geoOperations.add(key, new Point(longitude, latitude), member);
                System.out.println(member);
//                log.info("저장성공", member);
            } catch (Exception e) {
                log.error("저장실패", e.getMessage());
            }
        }
    }

    private String serializePoliceDto(PoliceDto policeDto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(policeDto);
    }

    private PoliceDto deserializePoliceDto(String value) throws JsonProcessingException {
        return objectMapper.readValue(value, PoliceDto.class);
    }
}
