package com.example.majoong.path.Repository;

import com.example.majoong.path.domain.Node;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NodeRepository extends JpaRepository<Node, Long> {

    // postGIS 에서 입력 좌표 반경 1km에서 가장 가까운 도로 노드 검색
    /**
     * postGIS에 도로망 데이터 넣는 필드 형식에 맞춰서 아래쿼리문 수정하기
     * id는 공간 인덱스
     * 4326으로 WGS84 좌표계 설정
     * 검색 속도를 고려해서 where절 추가 고려하기
     */
    @Query(value = "SELECT id, geom " +
            "FROM node " +
            "ORDER BY geom <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326) " +
            "LIMIT 1",
            nativeQuery = true)
    Node findNearestNode(@Param("lng") Double lng, @Param("lat") Double lat);
}